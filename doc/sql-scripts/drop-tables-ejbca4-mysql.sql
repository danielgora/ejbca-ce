alter table AccessRulesData drop foreign key FKABB4C1DFD8AEA20;
alter table AdminEntityData drop foreign key FKD9A99EBCB370315D;
drop table if exists AccessRulesData;
drop table if exists AdminEntityData;
drop table if exists AdminGroupData;
drop table if exists AdminPreferencesData;
drop table if exists ApprovalData;
drop table if exists AuthorizationTreeUpdateData;
drop table if exists CAData;
drop table if exists CRLData;
drop table if exists CertReqHistoryData;
drop table if exists CertificateData;
drop table if exists Base64CertData;
drop table if exists CertificateProfileData;
drop table if exists EndEntityProfileData;
drop table if exists GlobalConfigurationData;
drop table if exists HardTokenCertificateMap;
drop table if exists HardTokenData;
drop table if exists HardTokenIssuerData;
drop table if exists HardTokenProfileData;
drop table if exists HardTokenPropertyData;
drop table if exists KeyRecoveryData;
drop table if exists LogConfigurationData;
drop table if exists LogEntryData;
drop table if exists PublisherData;
drop table if exists PublisherQueueData;
drop table if exists ServiceData;
drop table if exists UserData;
drop table if exists UserDataSourceData;
