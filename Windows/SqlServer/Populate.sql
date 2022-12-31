USE WIFILOCATION

GO

SELECT * FROM TempData

GO
DROP TABLE TempData;
GO
CREATE TABLE TempData
(
	BSSID VARCHAR(40),
	SSID VARCHAR(53),
	LEVEL INT,
	CAPABILITIES VARCHAR(228),
	FREQUENCY INT,
	Latitude REAL,
	Longitude REAL,
	Altitude REAL,
		Timestamp DateTime,
	HA REAL,
	VA REAL,
	SessionID Uniqueidentifier,
	ClientID UNIQUEIDENTIFIER,
	LocationLabel VARCHAR(50)
)
GO
TRUNCATE TABLE TempData
GO
SELECT * FROM TempData
GO
BULK INSERT  WIFILOCATION.dbo.TempData
FROM "C:\shares\projects\j2inet\wioLocator\Windows\SqlServer\dataSource.csv"
WITH
(
	 DATAFILETYPE = 'char'
	 ,FIRSTROW=2
	,FIELDTERMINATOR = ','
);
GO

SELECT * FROM TempData;


GO

DECLARE @UniqueBSSIDCount INT;
SELECT DISTINCT BSSID, SSID from TempData where SSID = 'j2i.net'
SELECT COUNT(*) FROM (SELECT DISTINCT BSSID FROM TempData)as DB
GO

DELETE * from WiFiSample
GO
INSERT INTO WiFiSample(
	Contributor_ID,
	SessionID,
	ClientID, 
	BSSID, 
	SSID, 
	LEVEL, 
	CAPABILITIES, 
	FREQUENCY, 
	RecordedTime, 
	Location, 
	Altitude, 
	HA, 
	VA,
	LocationLabel)
SELECT 1, ISNULL(SessionID,'00000000-0000-0000-0000-000000000000'), ISNULL(ClientID,'00000000-0000-0000-0000-000000000000'), BSSID, SSID, LEVEL, Capabilities, Frequency, TimeStamp , geography::STPointFromText('POINT(' + CAST(Longitude AS VARCHAR(20)) + ' ' + CAST(Latitude AS VARCHAR(20)) + ')', 4326), Altitude, HA, VA, LocationLabel
FROM TEMPDATA
GO
SELECT * FROM WifiSample

