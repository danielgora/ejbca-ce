update UserData set certificateProfileId=9 where username='tomcat' and certificateProfileId=1;
ALTER TABLE CertificateData ADD tag VARCHAR(256) DEFAULT NULL;
ALTER TABLE CertificateData ADD certificateProfileId INTEGER DEFAULT 0;
ALTER TABLE CertificateData ADD updateTime DECIMAL(20) NOT NULL DEFAULT 0;
UPDATE CertificateData SET certificateProfileId=0;

-- Add rowVersion column to all tables
ALTER TABLE AccessRulesData ADD COLUMN rowVersion INTEGER DEFAULT 0; 
ALTER TABLE AdminEntityData ADD COLUMN rowVersion INTEGER DEFAULT 0;
ALTER TABLE AdminGroupData ADD COLUMN rowVersion INTEGER DEFAULT 0;
ALTER TABLE AdminPreferencesData ADD COLUMN rowVersion INTEGER DEFAULT 0;
ALTER TABLE ApprovalData ADD COLUMN rowVersion INTEGER DEFAULT 0;
ALTER TABLE AuthorizationTreeUpdateData ADD COLUMN rowVersion INTEGER DEFAULT 0;
ALTER TABLE CAData ADD COLUMN rowVersion INTEGER DEFAULT 0;
ALTER TABLE CRLData ADD COLUMN rowVersion INTEGER DEFAULT 0;
ALTER TABLE CertReqHistoryData ADD COLUMN rowVersion INTEGER DEFAULT 0;
ALTER TABLE CertificateData ADD COLUMN rowVersion INTEGER DEFAULT 0;
ALTER TABLE CertificateProfileData ADD COLUMN rowVersion INTEGER DEFAULT 0;
ALTER TABLE EndEntityProfileData ADD COLUMN rowVersion INTEGER DEFAULT 0;
ALTER TABLE GlobalConfigurationData ADD COLUMN rowVersion INTEGER DEFAULT 0;
ALTER TABLE HardTokenCertificateMap ADD COLUMN rowVersion INTEGER DEFAULT 0;
ALTER TABLE HardTokenData ADD COLUMN rowVersion INTEGER DEFAULT 0;
ALTER TABLE HardTokenIssuerData ADD COLUMN rowVersion INTEGER DEFAULT 0;
ALTER TABLE HardTokenProfileData ADD COLUMN rowVersion INTEGER DEFAULT 0;
ALTER TABLE HardTokenPropertyData ADD COLUMN rowVersion INTEGER DEFAULT 0;
ALTER TABLE KeyRecoveryData ADD COLUMN rowVersion INTEGER DEFAULT 0;
ALTER TABLE LogConfigurationData ADD COLUMN rowVersion INTEGER DEFAULT 0;
ALTER TABLE LogEntryData ADD COLUMN rowVersion INTEGER DEFAULT 0;
ALTER TABLE PublisherData ADD COLUMN rowVersion INTEGER DEFAULT 0;
ALTER TABLE ServiceData ADD COLUMN rowVersion INTEGER DEFAULT 0;
ALTER TABLE UserData ADD COLUMN rowVersion INTEGER DEFAULT 0;
ALTER TABLE UserDataSourceData ADD COLUMN rowVersion INTEGER DEFAULT 0;

-- Add rowProtection column to all tables
ALTER TABLE AccessRulesData ADD COLUMN rowProtection LONG DEFAULT NULL; 
ALTER TABLE AdminEntityData ADD COLUMN rowProtection LONG DEFAULT NULL;
ALTER TABLE AdminGroupData ADD COLUMN rowProtection LONG DEFAULT NULL;
ALTER TABLE AdminPreferencesData ADD COLUMN rowProtection LONG DEFAULT NULL;
ALTER TABLE ApprovalData ADD COLUMN rowProtection LONG DEFAULT NULL;
ALTER TABLE AuthorizationTreeUpdateData ADD COLUMN rowProtection LONG DEFAULT NULL;
ALTER TABLE CAData ADD COLUMN rowProtection LONG DEFAULT NULL;
ALTER TABLE CRLData ADD COLUMN rowProtection LONG DEFAULT NULL;
ALTER TABLE CertReqHistoryData ADD COLUMN rowProtection LONG DEFAULT NULL;
ALTER TABLE CertificateData ADD COLUMN rowProtection LONG DEFAULT NULL;
ALTER TABLE CertificateProfileData ADD COLUMN rowProtection LONG DEFAULT NULL;
ALTER TABLE EndEntityProfileData ADD COLUMN rowProtection LONG DEFAULT NULL;
ALTER TABLE GlobalConfigurationData ADD COLUMN rowProtection LONG DEFAULT NULL;
ALTER TABLE HardTokenCertificateMap ADD COLUMN rowProtection LONG DEFAULT NULL;
ALTER TABLE HardTokenData ADD COLUMN rowProtection LONG DEFAULT NULL;
ALTER TABLE HardTokenIssuerData ADD COLUMN rowProtection LONG DEFAULT NULL;
ALTER TABLE HardTokenProfileData ADD COLUMN rowProtection LONG DEFAULT NULL;
ALTER TABLE HardTokenPropertyData ADD COLUMN rowProtection LONG DEFAULT NULL;
ALTER TABLE KeyRecoveryData ADD COLUMN rowProtection LONG DEFAULT NULL;
ALTER TABLE LogConfigurationData ADD COLUMN rowProtection LONG DEFAULT NULL;
ALTER TABLE LogEntryData ADD COLUMN rowProtection LONG DEFAULT NULL;
ALTER TABLE PublisherData ADD COLUMN rowProtection LONG DEFAULT NULL;
ALTER TABLE ServiceData ADD COLUMN rowProtection LONG DEFAULT NULL;
ALTER TABLE UserData ADD COLUMN rowProtection LONG DEFAULT NULL;
ALTER TABLE UserDataSourceData ADD COLUMN rowProtection LONG DEFAULT NULL;
