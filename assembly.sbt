import AssemblyKeys._ // put this at the top of the file

assemblySettings

// your assembly settings here

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
{
  case x if x.contains(".class") => MergeStrategy.first
  case x if x.contains(".dtd") => MergeStrategy.first
  case x if x.contains(".xsd") => MergeStrategy.first
  case x if x.contains("plugin.properties") => MergeStrategy.filterDistinctLines
  case x => old(x)
}
}
