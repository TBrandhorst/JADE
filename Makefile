# Makefile for JADE project

VERSION    = 1.4
PACKAGE    = JADE

ZIP = jar
ZIPFLAGS = cMvf
ZIPEXT = zip

ROOTDIR = $(shell pwd)
ROOTNAME = $(shell basename $(ROOTDIR))
DOCDIR  = $(ROOTDIR)/doc
SRCDIR  = $(ROOTDIR)/src
CLSDIR  = $(ROOTDIR)/classes
LIBDIR  = $(ROOTDIR)/lib
LIBNAME = jade.jar
LIBTOOLSNAME = jadeTools.jar
LIBBASE64NAME = Base64.jar
LIBAPPLETNAME = appletArchive.jar
EXAMPLESDIR = $(SRCDIR)/examples
DEMODIR = $(SRCDIR)/demo
MAKE = make

JC = javac
JFLAGS = -deprecation -O -d $(CLSDIR)

BATCH_DIST = $(ROOTDIR)/makedist.bat
BATCH_ALL = $(ROOTDIR)/makejade.bat
BATCH_EXAMPLES = $(ROOTDIR)/makeexamples.bat
BATCH_JESSEXAMPLE = $(ROOTDIR)/makejessexample.bat
BATCH_DOC = $(ROOTDIR)/makedoc.bat
BATCH_LIB = $(ROOTDIR)/makelib.bat
BATCH_CLEAN = $(ROOTDIR)/clean.bat

COMMON_FILES = jade/README jade/License jade/COPYING

export VERSION
export PACKAGE
export ROOTDIR
export ROOTNAME
export DOCDIR
export SRCDIR
export CLSDIR
export LIBDIR
export LIBNAME
export LIBTOOLSNAME
export LIBBASE64NAME
export EXAMPLESDIR
export DEMODIR
export MAKE

export JC
export JFLAGS

export BATCH_ALL
export BATCH_EXAMPLES
export BATCH_JESSEXAMPLE
export BATCH_DOC
export BATCH_LIB
export BATCH_CLEAN

# The following targets are not file names
.PHONY: all clean doc src lib examples dist

all: $(CLSDIR)
	cd $(SRCDIR); $(MAKE) all
	@echo Sources built

doc: $(DOCDIR) clean
	cd $(DOCDIR); $(MAKE) all
	@echo HTML documentation built

lib:
	cd $(LIBDIR); $(MAKE) all
	@echo Libraries built

examples: $(CLSDIR)
	cd $(EXAMPLESDIR); $(MAKE) all
	cd $(DEMODIR); $(MAKE) all
	@echo Examples and demo applications built.

jessexample: $(CLSDIR)
	cd $(EXAMPLESDIR)/jess; $(MAKE) all
	@echo Jess example built.

clean:
	rm -f `find . -name '*~'`
	rm -f `find . -name '#*#'`
	rm -f `find . -name JADE.IOR`
	rm -f `find . -name JADE.URL`
	rm -fr $(CLSDIR)/*
	cd $(DOCDIR); $(MAKE) clean
	cd $(LIBDIR); $(MAKE) clean
	cd $(EXAMPLESDIR); $(MAKE) clean
	cd $(DEMODIR); $(MAKE) clean

realclean: clean
	cd $(SRCDIR); $(MAKE) idlclean
	cd $(SRCDIR); $(MAKE) jjclean
	rm -f $(BATCH_ALL)
	rm -f $(BATCH_DIST)
	rm -f $(BATCH_EXAMPLES)
	rm -f $(BATCH_JESSEXAMPLE)
	rm -f $(BATCH_DOC)
	rm -f $(BATCH_LIB)
	rm -f $(BATCH_CLEAN)

dist:	clean idl parsers batch srcarchive examplesarchive docarchive binarchive


idl:
	cd $(SRCDIR); $(MAKE) idl

parsers:
	cd $(SRCDIR); $(MAKE) parsers

idlclean: clean
	cd $(SRCDIR); $(MAKE) idlclean

jjclean: clean
	cd $(SRCDIR); $(MAKE) jjclean

srcarchive: $(CLSDIR) $(DOCDIR)
	cd $(ROOTDIR)/..; \
	$(ZIP) $(ZIPFLAGS) $(PACKAGE)-src-$(VERSION).$(ZIPEXT) $(COMMON_FILES) jade/*.bat jade/Makefile jade/src/Makefile jade/src/*.idl jade/src/jade; \
	cd $(ROOTDIR)

examplesarchive:
	cd $(ROOTDIR)/..; \
	$(ZIP) $(ZIPFLAGS) $(PACKAGE)-examples-$(VERSION).$(ZIPEXT) $(COMMON_FILES) jade/src/examples jade/src/demo; \
	cd $(ROOTDIR)

docarchive: $(DOCDIR) doc
	cd $(ROOTDIR)/..; \
	$(ZIP) $(ZIPFLAGS) $(PACKAGE)-doc-$(VERSION).$(ZIPEXT) $(COMMON_FILES) jade/doc; \
	cd $(ROOTDIR)

binarchive: $(CLSDIR) $(DOCDIR) all lib
	cd $(ROOTDIR)/..; \
	$(ZIP) $(ZIPFLAGS) $(PACKAGE)-bin-$(VERSION).$(ZIPEXT) $(COMMON_FILES) jade/lib jade/src/starlight; \
	cd $(ROOTDIR)

batch: $(CLSDIR) $(DOCDIR)
	rm -f $(BATCH_DIST)
	echo '@REM ===============================================================' > $(BATCH_DIST)
	echo '@REM =           Generated by JADE Makefile. DO NOT EDIT           =' >> $(BATCH_DIST)
	echo '@REM ===============================================================' >> $(BATCH_DIST)
	echo >> $(BATCH_DIST)

	echo 'cd ..;' >> $(BATCH_DIST)
	echo '$(ZIP) $(ZIPFLAGS) $(PACKAGE)-src-$(VERSION).$(ZIPEXT) $(subst /,\,$(COMMON_FILES)) jade\*.bat jade\Makefile jade\src\Makefile jade\src\*.idl jade\src\jade' >> $(BATCH_DIST)
	echo 'cd jade' >> $(BATCH_DIST)

	echo 'cd ..' >> $(BATCH_DIST)
	echo '$(ZIP) $(ZIPFLAGS) $(PACKAGE)-examples-$(VERSION).$(ZIPEXT) $(subst /,\,$(COMMON_FILES)) jade\src\examples jade\src\demo' >> $(BATCH_DIST)
	echo 'cd jade' >> $(BATCH_DIST)

	echo 'call $(subst /,\,$(subst $(ROOTDIR),.,$(BATCH_DOC)))' >> $(BATCH_DIST)
	echo 'cd ..' >> $(BATCH_DIST)
	echo '$(ZIP) $(ZIPFLAGS) $(PACKAGE)-doc-$(VERSION).$(ZIPEXT) $(subst /,\,$(COMMON_FILES)) jade\doc' >> $(BATCH_DIST)
	echo 'cd jade' >> $(BATCH_DIST)

	echo 'call $(subst /,\,$(subst $(ROOTDIR),.,$(BATCH_ALL)))' >> $(BATCH_DIST)
	echo 'call $(subst /,\,$(subst $(ROOTDIR),.,$(BATCH_LIB)))' >> $(BATCH_DIST) 
	echo 'cd ..' >> $(BATCH_DIST)
	echo '$(ZIP) $(ZIPFLAGS) $(PACKAGE)-bin-$(VERSION).$(ZIPEXT) $(subst /,\,$(COMMON_FILES)) jade\lib jade\src\starlight' >> $(BATCH_DIST)
	echo 'cd jade' >> $(BATCH_DIST)

	rm -f $(BATCH_CLEAN)
	echo '@REM ===============================================================' > $(BATCH_CLEAN)
	echo '@REM =           Generated by JADE Makefile. DO NOT EDIT           =' >> $(BATCH_CLEAN)
	echo '@REM ===============================================================' >> $(BATCH_CLEAN)
	echo >> $(BATCH_CLEAN)
	echo 'rmdir /S /Q classes\*' >> $(BATCH_CLEAN) # For Win NT 4
	echo 'deltree /Y classes\*' >> $(BATCH_CLEAN)  # For Win 95/98

	cd $(SRCDIR); $(MAKE) batch
	cd $(EXAMPLESDIR); $(MAKE) batch
	cd $(EXAMPLESDIR)/jess; $(MAKE) batch
	cd $(DEMODIR); $(MAKE) batch
	cd $(DOCDIR); $(MAKE) batch
	cd $(LIBDIR); $(MAKE) batch

$(CLSDIR):
	mkdir $(CLSDIR)

$(DOCDIR):
	mkdir $(DOCDIR)
