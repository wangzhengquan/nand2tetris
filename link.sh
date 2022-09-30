#!/bin/bash

# https://www.diskinternals.com/linux-reader/bash-string-ends-with/

function buildln() {
  local dir = ${1}
 # ls ${1}
  for file in `ls ${dir}`
  do
    ln -s ${dir}/${file}
    echo ${dir}/${file}
  done
}

function renameXmlFile() {
  local dir=${1}
 # ls ${1}
  #cd ${1}
  for file in `ls ${dir}`
  do
  	if [[ -d ${dir}/$file ]]; then
		    renameXmlFile ${dir}/$file
		elif [[ -f ${dir}/$file ]]; then
		    if [[ $file == *.xml ]]
		    then
		    	
		    	 mv ${dir}/${file} "${dir}/${file}.cmp"
           #echo rename ${dir}/${file}
		  	fi
		else
		    echo "$file is not valid"
		    exit 1
		fi
  done
}

# buildln ~/wk/nand2tetris/projects/01
renameXmlFile ~/wk/nand2tetris/projects/10
