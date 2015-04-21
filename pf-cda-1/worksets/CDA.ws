<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE Workset SYSTEM "http://dependency-analyzer.org/schema/dtd/workset-1.7.dtd">
<Workset version="7">
  <WorksetName>CDA</WorksetName>
  <Description>This workset contains all JAR files that comprise the Class Dependency Analyzer. It can be used as a starting point to get familiar with this tool.</Description>
  <Options auto-reload="no" />
  <Classpath shortContainerNames="yes">
    <ClasspathPart type="bin-class">{CDA_HOME}/*.jar</ClasspathPart>
    <ClasspathPart type="bin-class">{CDA_HOME}/ext/*.jar</ClasspathPart>
  </Classpath>
  <ViewFilters>
    <PatternFilter active="yes">java.*</PatternFilter>
    <PatternFilter active="yes">javax.*</PatternFilter>
    <PatternFilter active="yes">com.sun.*</PatternFilter>
  </ViewFilters>
  <IgnoreFilters>
    <PatternFilter active="yes">java.*</PatternFilter>
    <PatternFilter active="yes">javax.*</PatternFilter>
    <PatternFilter active="yes">com.sun.*</PatternFilter>
  </IgnoreFilters>
  <Architecture>
    <ComponentModel name="Default" />
  </Architecture>
</Workset>