GO
USE Master
GO
DROP TABLE  IF EXISTS  WIFISAMPLE;
GO
DROP DATABASE  IF EXISTS  Contributor
GO
DROP DATABASE  IF EXISTS  WIFILOCATION
GO
CREATE DATABASE WIFILOCATION;
GO 
Use WIFILOCATION;
GO
CREATE TABLE Contributor
(
	ID INT Identity PRIMARY KEY,
	DisplayName VARCHAR(64),
	IsEnabled BIT NOT NULL DEFAULT(0)
);
GO
INSERT INTO Contributor(DisplayName, IsEnabled) VALUES ('System', 1);
GO

GO
CREATE TABLE WiFiSample (
	WiFiSample_ID INT Identity PRIMARY KEY,
	Contributor_ID INT CONSTRAINT  FK_WiFiSample_Contributor  FOREIGN KEY  REFERENCES Contributor(ID),
	SessionID UNIQUEIDENTIFIER NOT NULL, 
	ClientID UNIQUEIDENTIFIER NOT NULL,
	BSSID CHAR(17) NOT NULL,
	SSID varchar(40),
	LEVEL INT,
	CAPABILITIES VARCHAR(128),
	FREQUENCY INT,
	RecordedTime DATETIME NOT NULL,
	Location geography,
	altitude REAL,
	HA REAL,
	VA Real,
	LocationLabel VARCHAR(64)
);
GO
CREATE PROCEDURE AddWifiSample(
	@Contributor_ID INT ,
	@SessionID UNIQUEIDENTIFIER NOT NULL, 
	@ClientID UNIQUEIDENTIFIER NOT NULL,
	@BSSID CHAR(17) NOT NULL,
	@SSID varchar(40),
	@LEVEL INT,
	@CAPABILITIES VARCHAR(128),
	@FREQUENCY INT,
	@RecordedTime DATETIME NOT NULL,
	@Location geography,
	@altitude REAL,
	@HA REAL,
	@VA Real,
	@LocationLabel VARCHAR(64)
)
AS
	SET NOCOUNT ON;

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
VALUES (
	@Contributor_ID  ,
	@SessionID, 
	@ClientID ,
	@BSSID ,
	@SSID ,
	@LEVEL ,
	@CAPABILITIES ,
	@FREQUENCY ,
	@RecordedTime ,
	@Location ,
	@altitude ,
	@HA ,
	@VA ,
	@LocationLabel );

GO
DECLARE @SystemID INT 
SELECT @SystemID=ID from Contributor WHERE DisplayName='System';

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
VALUES (
	@SystemID, 
	'9329b51f-af7f-44d8-a0ad-53acf2eaa189', 
	'14db24a3-db2c-4f31-8171-b3774a26f080', 
	'ae:46:8d:28:d3:a8',
	'VSS-Guest-WiFi', 
	-73, 
	'[ESS]', 
	5220, 
	'2022-12-28 13:23:50', 
	geography::STPointFromText('POINT(' + CAST(-84.3523559 AS VARCHAR(20)) + ' ' + CAST(33.5752121 AS VARCHAR(20)) + ')', 4326), 
	239.4, 
	13.907, 
	0.9325592, 
	'Southlake Mall');
GO

SELECT *, Location.Lat, Location.Long FROM WifiSample


