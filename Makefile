-include Makefile.inc

# doc.update=false is necessary to avoid trying to pull documentation from the
# primekey server.
JAVAOPTS += -Ddoc.update=false
# This is necessary (ZZZ still?) to have ejbca find what type of application
# server we're using
export APPSRV_HOME=/opt/bitnami/wildfly

# Path to the Adax git repo for the bitnami docker container for ejbca
export CONTAINER_HOME=/home/dg/ejbca/adax-docker-ejbca

export PREINSTALLDIR=$(CONTAINER_HOME)/7/debian-10/prebuildfs
export INSTALLDIR=$(PREINSTALLDIR)/opt/bitnami/pkgcache

.PHONY: all build clean install tags cscope

all: build

update_ver:
	ant $(JAVAOPTS) update-edition update-gitrev

build: update_ver
	ant $(JAVAOPTS) build-bitnami

debug:
	$(MAKE) JAVAOPTS=-debug build

clean:
	ant clean
	rm -f tags cscope.files

install: build Makefile.inc
	@echo -e "\nCopying tarball to $(INSTALLDIR)\n"
	@[[ -d $(PREINSTALLDIR) ]] && \
		mkdir -p $(INSTALLDIR) && \
		cp -p ejbca-*.tar.gz* $(INSTALLDIR)

tags:
	ctags -R . 2>/dev/null

cscope:
	find . -iname '*.java' > cscope.files
	cscope -b

Makefile.inc: configure
	./configure
