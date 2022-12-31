// WiFiScanner.cpp : This file contains the 'main' function. Program execution begins and ends there.
//

#include "pch.h"
#include <iostream>
#include <ostream>
#include <Windows.h>
#include <Wlanapi.h>
#include <cstring>
#include <cstdlib>
#include <iostream>
#include <sstream>>

#pragma comment(lib, "Wlanapi")


struct WlanCallbackContext
{
    HANDLE wlanHandle;
    GUID interfaceGuid;
    HANDLE scanCompleteEvent;
    std::string ComputerName;
};
typedef  WlanCallbackContext* PWlanCallbackContext;


std::string FormatBssid(UCHAR* bssid)
{
    char bssidstr[48];
    sprintf_s(bssidstr, "%02x:%02x:%02x:%02x:%02x:%02x", bssid[0], bssid[1], bssid[2], bssid[3], bssid[4], bssid[5]);

    return std::string(bssidstr);
}


void WlanNotificationCallback(PWLAN_NOTIFICATION_DATA notificationData, PVOID contextData)
{
    DWORD result;
    PWLAN_BSS_LIST pBssList = NULL;
    PWlanCallbackContext context = (PWlanCallbackContext)contextData;
    
    switch (notificationData->NotificationSource)
    {
    case WLAN_NOTIFICATION_SOURCE_ACM:

        result = WlanGetNetworkBssList(context->wlanHandle, &context->interfaceGuid,
            NULL /*&pConnectInfo->wlanAssociationAttributes.dot11Ssid */,
            dot11_BSS_type_any,
            TRUE, NULL, &pBssList);
        if (ERROR_SUCCESS == result)
        {
            for (auto i = 0; i < pBssList->dwNumberOfItems; ++i)
            {
                auto item = pBssList->wlanBssEntries[i];
                std::cout << context->ComputerName << ", ";
                std::cout << FormatBssid(item.dot11Bssid) << ", ";
                std::cout << item.dot11Ssid.ucSSID << ", ";
                std::cout << item.ulChCenterFrequency << ", ";
                std::cout << item.lRssi << ", ";
                std::cout << ((item.usCapabilityInformation & 0x01) ? "[+ESS]" : "[-ESS]");
                std::cout << ((item.usCapabilityInformation & 0x02) ? "[+IBSS]" : "[-IBSS]");
                std::cout << ((item.usCapabilityInformation & 0x04) ? "[+CF_Pollable]" : "[-CF_Pollable]");
                std::cout << ((item.usCapabilityInformation & 0x08) ? "[+CF_PollRequest]" : "[-CF_PollRequest]");
                std::cout << ((item.usCapabilityInformation & 0x10) ? "[+Privacy]" : "[-Privacy]");
                std::cout << ", ";
                for (int k = 0; k < item.wlanRateSet.uRateSetLength; ++k)
                {
                    std::cout << "[" << item.wlanRateSet.usRateSet[k] << "]";
                }
                std::cout << ", ";
                std::cout << item.ullHostTimestamp << ", " << item.ullTimestamp << ", ";
                switch (item.dot11BssType)
                {
                case dot11_BSS_type_infrastructure: std::cout << "infastructure"; break;
                case dot11_BSS_type_independent: std::cout << "independend"; break;
                case dot11_BSS_type_any:std::cout << "any"; break;
                default: std::cout << "";
                }

                std::cout << std::endl;

            }
            WlanFreeMemory(pBssList);
        }
    

        break;
    default:
        break;
    }
    
    SetEvent(context->scanCompleteEvent);
}




int main()
{
    DWORD version;
    //HANDLE wlanHandle;
    DWORD result;
    PWLAN_BSS_LIST pBssList = NULL;
    int retVal = 0;

    WlanCallbackContext context = { 0 };
    context.scanCompleteEvent = CreateEvent(NULL, FALSE, NULL, NULL);

    std::wstring ComputerName;
    WCHAR ComputerNameBuffer[1024] = { 0 };
    CHAR ComputerNameBufferSmall[1024] = { 0 };
    DWORD ComputerNameSize = 1024;
    GetComputerName(ComputerNameBuffer, &ComputerNameSize);
    WideCharToMultiByte(CP_UTF8, WC_ERR_INVALID_CHARS, ComputerNameBuffer, ComputerNameSize,(LPSTR) ComputerNameBufferSmall, 1024, NULL, NULL);
    context.ComputerName = ComputerNameBufferSmall;



    if (ERROR_SUCCESS == WlanOpenHandle(2, nullptr, &version, &context.wlanHandle))
    {
        result = WlanRegisterNotification(context.wlanHandle, WLAN_NOTIFICATION_SOURCE_ACM, TRUE, (WLAN_NOTIFICATION_CALLBACK)WlanNotificationCallback, &context, NULL, NULL);
 
        switch (result)
        {
        case ERROR_INVALID_PARAMETER: std::cout << "invalud parameter"; break;
        case ERROR_INVALID_HANDLE: std::cout << "invalid handle"; break;
        default:break;
        }
        
        PWLAN_INTERFACE_INFO_LIST interfaceList;
        if (ERROR_SUCCESS != (result = WlanEnumInterfaces(context.wlanHandle, NULL, &interfaceList)))
        {
            wprintf(L"unable to enumerate wireless interfaces. Error %d", result);
        }
        else
        {
            std::cout << "Host, BSSID, Access Point Name, Frequency, RSSI, Capabilities, Rateset, Host Timestamp, Timestamp, BSS Type" << std::endl;

            for (int i = 0; i < (int)interfaceList->dwNumberOfItems; i++)
            {
                PWLAN_INTERFACE_INFO wirelessInterface;
                wirelessInterface = (WLAN_INTERFACE_INFO*)&interfaceList->InterfaceInfo[i];
                if (wirelessInterface->isState == wlan_interface_state_connected)
                {
                    context.interfaceGuid = wirelessInterface->InterfaceGuid;
                     if (ERROR_SUCCESS != (result = WlanScan(context.wlanHandle, &context.interfaceGuid, NULL, NULL, NULL)))
                    {
                        std::cout << "Scan failed" << std::endl;
                        retVal = 1;
                    }
                    else 
                     {
                        ResetEvent(context.scanCompleteEvent);
                        WaitForSingleObject(context.scanCompleteEvent, INFINITE);
                     }
                } 
            }
            WlanFreeMemory(interfaceList);
            WlanRegisterNotification(context.wlanHandle, WLAN_NOTIFICATION_SOURCE_ACM, TRUE, (WLAN_NOTIFICATION_CALLBACK)NULL, NULL, NULL, NULL);

        }
        std::string str;
        WlanCloseHandle(context.wlanHandle, NULL);
    }
    return retVal;
}

