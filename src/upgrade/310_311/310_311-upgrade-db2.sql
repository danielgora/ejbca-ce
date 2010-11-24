
-- Perform data-type changes to have size consistency over all databases
--  ApprovalData.requestdata is currently VARCHAR(8000), but is defined as CLOB on other databases
--  ApprovalData.approvaldata is currently VARCHAR(4000), but is defined as CLOB on other databases
ALTER TABLE ApprovalData ADD tmprequestdata CLOB DEFAULT NULL;
ALTER TABLE ApprovalData ADD tmpapprovaldata CLOB DEFAULT NULL;
UPDATE ApprovalData SET tmprequestdata=requestdata;
UPDATE ApprovalData SET tmpapprovaldata=approvaldata;
ALTER TABLE ApprovalData DROP COLUMN requestdata;
ALTER TABLE ApprovalData DROP COLUMN approvaldata;
ALTER TABLE ApprovalData ADD requestdata CLOB DEFAULT NULL;
ALTER TABLE ApprovalData ADD approvaldata CLOB DEFAULT NULL;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ApprovalData');
UPDATE ApprovalData SET requestdata=tmprequestdata;
UPDATE ApprovalData SET approvaldata=tmpapprovaldata;
ALTER TABLE ApprovalData DROP COLUMN tmprequestdata;
ALTER TABLE ApprovalData DROP COLUMN tmpapprovaldata;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ApprovalData');

--  CertificateData.base64Cert is currently VARCHAR(8000), but is defined as CLOB on other databases
ALTER TABLE CertificateData ADD tmpbase64Cert CLOB DEFAULT NULL;
UPDATE CertificateData SET tmpbase64Cert=base64Cert;
ALTER TABLE CertificateData DROP COLUMN base64Cert;
ALTER TABLE CertificateData ADD base64Cert CLOB DEFAULT NULL;
CALL SYSPROC.ADMIN_CMD('REORG TABLE CertificateData');
UPDATE CertificateData SET base64Cert=tmpbase64Cert;
ALTER TABLE CertificateData DROP COLUMN tmpbase64Cert;
CALL SYSPROC.ADMIN_CMD('REORG TABLE CertificateData');

--  KeyRecoveryData.keyData is currently VARCHAR(8000), but is defined as CLOB on other databases
ALTER TABLE KeyRecoveryData ADD tmpkeyData CLOB DEFAULT NULL;
UPDATE KeyRecoveryData SET tmpkeyData=keyData;
ALTER TABLE KeyRecoveryData DROP COLUMN keyData;
ALTER TABLE KeyRecoveryData ADD keyData CLOB DEFAULT NULL;
CALL SYSPROC.ADMIN_CMD('REORG TABLE KeyRecoveryData');
UPDATE KeyRecoveryData SET keyData=tmpkeyData;
ALTER TABLE KeyRecoveryData DROP COLUMN tmpkeyData;
CALL SYSPROC.ADMIN_CMD('REORG TABLE KeyRecoveryData');

-- ServiceData gets two new columns
ALTER TABLE ServiceData ADD COLUMN nextRunTimeStamp BIGINT NOT NULL WITH DEFAULT 0;
ALTER TABLE ServiceData ADD COLUMN runTimeStamp BIGINT NOT NULL WITH DEFAULT 0;
CALL SYSPROC.ADMIN_CMD('REORG TABLE ServiceData');

-- HardTokenPropertyData.id is limited to VARCHAR(80), but regular VARCHAR on other databases.
ALTER TABLE HardTokenPropertyData ADD tmpid VARCHAR(254) NOT NULL DEFAULT '';
UPDATE HardTokenPropertyData SET tmpid=id;
ALTER TABLE HardTokenPropertyData DROP PRIMARY KEY;
ALTER TABLE HardTokenPropertyData DROP COLUMN id;
ALTER TABLE HardTokenPropertyData ADD id VARCHAR(254) NOT NULL DEFAULT '';
CALL SYSPROC.ADMIN_CMD('REORG TABLE HardTokenPropertyData');
ALTER TABLE HardTokenPropertyData ADD PRIMARY KEY(id);
UPDATE HardTokenPropertyData SET id=tmpid;
ALTER TABLE HardTokenPropertyData DROP COLUMN tmpid;
CALL SYSPROC.ADMIN_CMD('REORG TABLE HardTokenPropertyData');

-- Add rowVersion column to all tables
ALTER TABLE AccessRulesData ADD COLUMN rowVersion INTEGER WITH DEFAULT 0;
ALTER TABLE AdminEntityData ADD COLUMN rowVersion INTEGER WITH DEFAULT 0;
ALTER TABLE AdminGroupData ADD COLUMN rowVersion INTEGER WITH DEFAULT 0;
ALTER TABLE AdminPreferencesData ADD COLUMN rowVersion INTEGER WITH DEFAULT 0;
ALTER TABLE ApprovalData ADD COLUMN rowVersion INTEGER WITH DEFAULT 0;
ALTER TABLE AuthorizationTreeUpdateData ADD COLUMN rowVersion INTEGER WITH DEFAULT 0;
ALTER TABLE CAData ADD COLUMN rowVersion INTEGER WITH DEFAULT 0;
ALTER TABLE CRLData ADD COLUMN rowVersion INTEGER WITH DEFAULT 0;
ALTER TABLE CertReqHistoryData ADD COLUMN rowVersion INTEGER WITH DEFAULT 0;
ALTER TABLE CertificateData ADD COLUMN rowVersion INTEGER WITH DEFAULT 0;
ALTER TABLE CertificateProfileData ADD COLUMN rowVersion INTEGER WITH DEFAULT 0;
ALTER TABLE EndEntityProfileData ADD COLUMN rowVersion INTEGER WITH DEFAULT 0;
ALTER TABLE GlobalConfigurationData ADD COLUMN rowVersion INTEGER WITH DEFAULT 0;
ALTER TABLE HardTokenCertificateMap ADD COLUMN rowVersion INTEGER WITH DEFAULT 0;
ALTER TABLE HardTokenData ADD COLUMN rowVersion INTEGER WITH DEFAULT 0;
ALTER TABLE HardTokenIssuerData ADD COLUMN rowVersion INTEGER WITH DEFAULT 0;
ALTER TABLE HardTokenProfileData ADD COLUMN rowVersion INTEGER WITH DEFAULT 0;
ALTER TABLE HardTokenPropertyData ADD COLUMN rowVersion INTEGER WITH DEFAULT 0;
ALTER TABLE KeyRecoveryData ADD COLUMN rowVersion INTEGER WITH DEFAULT 0;
ALTER TABLE LogConfigurationData ADD COLUMN rowVersion INTEGER WITH DEFAULT 0;
ALTER TABLE LogEntryData ADD COLUMN rowVersion INTEGER WITH DEFAULT 0;
ALTER TABLE PublisherData ADD COLUMN rowVersion INTEGER WITH DEFAULT 0;
ALTER TABLE ServiceData ADD COLUMN rowVersion INTEGER WITH DEFAULT 0;
ALTER TABLE UserData ADD COLUMN rowVersion INTEGER WITH DEFAULT 0;
ALTER TABLE UserDataSourceData ADD COLUMN rowVersion INTEGER WITH DEFAULT 0;

-- Add rowProtection column to all tables
ALTER TABLE AccessRulesData ADD COLUMN rowProtection CLOB(10K) DEFAULT NULL; 
ALTER TABLE AdminEntityData ADD COLUMN rowProtection CLOB(10K) DEFAULT NULL;
ALTER TABLE AdminGroupData ADD COLUMN rowProtection CLOB(10K) DEFAULT NULL;
ALTER TABLE AdminPreferencesData ADD COLUMN rowProtection CLOB(10K) DEFAULT NULL;
ALTER TABLE ApprovalData ADD COLUMN rowProtection CLOB(10K) DEFAULT NULL;
ALTER TABLE AuthorizationTreeUpdateData ADD COLUMN rowProtection CLOB(10K) DEFAULT NULL;
ALTER TABLE CAData ADD COLUMN rowProtection CLOB(10K) DEFAULT NULL;
ALTER TABLE CRLData ADD COLUMN rowProtection CLOB(10K) DEFAULT NULL;
ALTER TABLE CertReqHistoryData ADD COLUMN rowProtection CLOB(10K) DEFAULT NULL;
ALTER TABLE CertificateData ADD COLUMN rowProtection CLOB(10K) DEFAULT NULL;
ALTER TABLE CertificateProfileData ADD COLUMN rowProtection CLOB(10K) DEFAULT NULL;
ALTER TABLE EndEntityProfileData ADD COLUMN rowProtection CLOB(10K) DEFAULT NULL;
ALTER TABLE GlobalConfigurationData ADD COLUMN rowProtection CLOB(10K) DEFAULT NULL;
ALTER TABLE HardTokenCertificateMap ADD COLUMN rowProtection CLOB(10K) DEFAULT NULL;
ALTER TABLE HardTokenData ADD COLUMN rowProtection CLOB(10K) DEFAULT NULL;
ALTER TABLE HardTokenIssuerData ADD COLUMN rowProtection CLOB(10K) DEFAULT NULL;
ALTER TABLE HardTokenProfileData ADD COLUMN rowProtection CLOB(10K) DEFAULT NULL;
ALTER TABLE HardTokenPropertyData ADD COLUMN rowProtection CLOB(10K) DEFAULT NULL;
ALTER TABLE KeyRecoveryData ADD COLUMN rowProtection CLOB(10K) DEFAULT NULL;
ALTER TABLE LogConfigurationData ADD COLUMN rowProtection CLOB(10K) DEFAULT NULL;
ALTER TABLE LogEntryData ADD COLUMN rowProtection CLOB(10K) DEFAULT NULL;
ALTER TABLE PublisherData ADD COLUMN rowProtection CLOB(10K) DEFAULT NULL;
ALTER TABLE ServiceData ADD COLUMN rowProtection CLOB(10K) DEFAULT NULL;
ALTER TABLE UserData ADD COLUMN rowProtection CLOB(10K) DEFAULT NULL;
ALTER TABLE UserDataSourceData ADD COLUMN rowProtection CLOB(10K) DEFAULT NULL;

CALL SYSPROC.ADMIN_CMD('REORG TABLE AccessRulesData');
CALL SYSPROC.ADMIN_CMD('REORG TABLE AdminEntityData');
CALL SYSPROC.ADMIN_CMD('REORG TABLE AdminGroupData');
CALL SYSPROC.ADMIN_CMD('REORG TABLE AdminPreferencesData');
CALL SYSPROC.ADMIN_CMD('REORG TABLE ApprovalData');
CALL SYSPROC.ADMIN_CMD('REORG TABLE AuthorizationTreeUpdateData');
CALL SYSPROC.ADMIN_CMD('REORG TABLE CAData');
CALL SYSPROC.ADMIN_CMD('REORG TABLE CRLData');
CALL SYSPROC.ADMIN_CMD('REORG TABLE CertReqHistoryData');
CALL SYSPROC.ADMIN_CMD('REORG TABLE CertificateData');
CALL SYSPROC.ADMIN_CMD('REORG TABLE CertificateProfileData');
CALL SYSPROC.ADMIN_CMD('REORG TABLE EndEntityProfileData');
CALL SYSPROC.ADMIN_CMD('REORG TABLE GlobalConfigurationData');
CALL SYSPROC.ADMIN_CMD('REORG TABLE HardTokenCertificateMap');
CALL SYSPROC.ADMIN_CMD('REORG TABLE HardTokenData');
CALL SYSPROC.ADMIN_CMD('REORG TABLE HardTokenIssuerData');
CALL SYSPROC.ADMIN_CMD('REORG TABLE HardTokenProfileData');
CALL SYSPROC.ADMIN_CMD('REORG TABLE HardTokenPropertyData');
CALL SYSPROC.ADMIN_CMD('REORG TABLE KeyRecoveryData');
CALL SYSPROC.ADMIN_CMD('REORG TABLE LogConfigurationData');
CALL SYSPROC.ADMIN_CMD('REORG TABLE LogEntryData');
CALL SYSPROC.ADMIN_CMD('REORG TABLE PublisherData');
CALL SYSPROC.ADMIN_CMD('REORG TABLE ServiceData');
CALL SYSPROC.ADMIN_CMD('REORG TABLE UserData');
CALL SYSPROC.ADMIN_CMD('REORG TABLE UserDataSourceData');
