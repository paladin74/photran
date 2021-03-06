# Generic makefile deletes all programs corresponding to .f90 files and rebuilds them
# Expects to run under bash
#
# Jeff Overbey
#
# Note - pattern substitution: $(patsubst %.f90,%.exe,$(wildcard *.f90))

# Command to run your Fortran compiler
FORTRAN=`which gfortran ifort 2>/dev/null | head -1`
DEBUGSWITCH=-g
INCLUDESWITCH=-I
LIBSWITCH=-L
CRAYSWITCH=-fcray-pointer
OPTSWITCH=-O4
OUTSWITCH=-o
EXEFILE=a.exe

# In each of these directories, all of the sources will be compiled into a single executable
#SRCDIRS=$(filter-out src-fortran2003, $(wildcard src-*))
SRCDIRS=$(wildcard src-*)

# Here we do not use $(wildcard) to delay expansion until the shell is invoked
SRCFILES=*.f*
OBJFILES=*.o *.obj
OTHERFILES=*.mod *.fwd *.map *.ydg
EXEFILES=$(EXEFILE) *.exe

all:	clean build

clean:
	@echo Cleaning output files
	@for dir in $(SRCDIRS) .; do \
	  cd $$dir; \
	  rm -f $(OBJFILES) >/dev/null 2>&1; \
	  rm -f $(OTHERFILES) >/dev/null 2>&1; \
	  rm -f $(EXEFILES) >/dev/null 2>&1; \
	  cd ..; \
	done

build:
	@if [ "$(FORTRAN)" == "" ]; then \
		echo "No Fortran compiler found; cannot compile photran-samples"; \
	else \
		echo "=====>" Using compiler $(FORTRAN); \
		for dir in $(SRCDIRS); do \
		  if [ -e $$dir/Makefile ]; then \
		    $(MAKE) -C $$dir; \
		  else \
		    echo "=====>" Begin compiling sources in $$dir; \
		    $(FORTRAN) $(DEBUGSWITCH) \
		               $(OUTSWITCH) $$dir/$(EXEFILE) \
		               $(INCLUDESWITCH). \
		               $(INCLUDESWITCH)$$dir \
		               $(LIBSWITCH). \
		               $(LIBSWITCH)$$dir \
		               $(OPTSWITCH) \
		               $(CRAYSWITCH) \
		               $$dir/$(SRCFILES); \
		    echo "<=====" Done compiling sources in $$dir; \
		  fi \
		done; \
		for dir in $(SRCDIRS) .; do \
		  cd $$dir; \
		  rm -f $(OBJFILES) >/dev/null 2>&1; \
		  rm -f $(OTHERFILES) >/dev/null 2>&1; \
		  cd ..; \
		done; \
	fi
