/*
Read an XML file and return its content with its alias references resolved and deleted.
And its import tags resolved too.
@precondition the file must be a well-formed BTML one.
@param path path of the XML file
@return the XML file content with its alias references resolved and deleted
*/
XML resolveAlias(String path){
  XML root = loadXML(path);
  XML[] aliases = root.getChildren("alias");
  
  for(XML alias : aliases){
    root.removeChild(alias);
    
    XML[] children = root.getChildren();
    for(int j=0; j<children.length; j++){
      if(children[j].getName().charAt(0) == '#')continue;
      findAndReplace(children[j], alias);
    }
  }
  
  ArrayList<XML> aliasesArrayList = getAliasesImports(root.getChildren("import"));
  
  for(XML alias : aliasesArrayList){
    XML[] children = root.getChildren();
    for(int j=0; j<children.length; j++){
      if(children[j].getName().charAt(0) == '#')continue;
      findAndReplace(children[j], alias);
    }
  }
  return root;
}

/*
Resolve a certain alias in a certain element if the tag match.
And in the children of the element.
@param element the element that we want to try to replace by the alias
@param alias the alias that we are trying to resolve
*/
void findAndReplace(XML element, XML alias){
  XML[] children = element.getChildren();
  
  // Try to resolve the alias in the children of the element
  for(int i=0; i<children.length; i++){
    if(children[i].getName().charAt(0) == '#')continue;
    findAndReplace(children[i], alias);
  }
  
  // Replace the element by the alias if they match
  if(element.getName() == alias.getChild(1).getName()){
    replace(element, alias);
  }
}

/*
Resolve the alias in the element
@param el the element that will be replaced by the alias
@param alias the alias that we are trying to resolve
*/
void replace(XML el, XML alias){
  XML elementSyntax = alias.getChild(1);
  XML aliasSyntax = alias.getChild(3);
  
  el.setName(aliasSyntax.getName());
  
  recursiveReplace(el, el, elementSyntax, aliasSyntax);
}

/*
Auxiliary function doing the actual resolving of the alias in an element
@param element the element of the original XML object, without any alias replacement
@param elementToUpdate the element that has to have his attributes replaced by those of the alias
@param elementSyntax canvas of the element that has to be replaced. It is the first part of an alias in the BTML langage
@param aliasSyntax canvas of the element that will replace. It is the seconde part of an alias in the BTML langage
*/
void recursiveReplace(XML element, XML elementToUpdate, XML elementSyntax, XML aliasSyntax){
  String[] aliasAttributes = aliasSyntax.listAttributes();

  for(int j=0; j < aliasAttributes.length; j++){
    elementToUpdate.setString(aliasAttributes[j], element.getString(aliasSyntax.getString(aliasAttributes[j]), elementSyntax.getString(aliasSyntax.getString(aliasAttributes[j]))));
  }
  
  XML[] children = aliasSyntax.getChildren();
  for(XML child: children){
    if(child.getName().charAt(0) == '#')continue;
    XML newChild = elementToUpdate.addChild(child.getName());
    recursiveReplace(element, newChild, elementSyntax, child);
  }
}

/*
Return the list of aliases that has been found in the files cited in the import
@param imports list of import tag as defined in the BTML langage
@return an ArrayList of aliases
*/
ArrayList<XML> getAliasesImports(XML[] imports){
  ArrayList<XML> aliases = new ArrayList<XML>();
  
  getAliasesImportsAux(imports, new ArrayList<String>(), aliases);
  
  return aliases;
}

void getAliasesImportsAux(XML[] imports, ArrayList<String> pathes, ArrayList<XML> aliases){
  String path;
  XML root;
  XML[] mainAliases;
  
  for(XML import_ : imports){
    path = import_.getString("path");
    
    if(pathes.contains(path))
    continue;
    
    pathes.add(path);
    
    root = loadXML(getPath(path));
    
    mainAliases = root.getChildren("alias");
    for(XML alias : mainAliases)
    aliases.add(alias);
    
    getAliasesImportsAux(root.getChildren("import"), pathes, aliases);
  }
}
