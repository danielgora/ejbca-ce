# Version: $Id$


drop database if exists ${url.path};
create database ${url.path};

revoke ALL PRIVILEGES, GRANT OPTION from '${database.username}'@'${url.host}';
DROP USER '${database.username}'@'${url.host}';

grant ALL on ${url.path}.* to '${database.username}'@'${url.host}' identified by '${database.password}';

flush privileges;
show grants for '${database.username}'@'${url.host}';
exit
