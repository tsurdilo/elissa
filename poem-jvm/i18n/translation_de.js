/**
 * @author sven.wagner-boysen
 * 
 * contains all strings for german language (de)
 * 
 */


// namespace
if(window.Repository == undefined) Repository = {};
if(window.Repository.I18N == undefined) Repository.I18N = {};

Repository.I18N.Language = "de"; //Pattern <ISO language code>_<ISO country code> in lower case!

// Repository strings

if(!Repository.I18N.Repository) Repository.I18N.Repository = {};

Repository.I18N.Repository.openIdSample = "ihre.openid.de";
Repository.I18N.Repository.sayHello = "Hallo";
Repository.I18N.Repository.login = "Anmelden";
Repository.I18N.Repository.logout = "Abmelden";

Repository.I18N.Repository.leftPanelTitle = "Modellorganisation";
Repository.I18N.Repository.rightPanelTitle = "Modellinfo";
Repository.I18N.Repository.bottomPanelTitle = "Kommentare";

// Plugins here
 
// NewModel Plugin
if(!Repository.I18N.NewModel) Repository.I18N.NewModel = {};

Repository.I18N.NewModel.name = "Neues Model";

// TypeFilter Plugin

if(!Repository.I18N.TypeFilter) Repository.I18N.TypeFilter = {};
Repository.I18N.TypeFilter.name = "Typ Filter";

if(!Repository.I18N.TagInfo) Repository.I18N.TagInfo = {};
Repository.I18N.TagInfo.name = "Tags"
Repository.I18N.TagInfo.deleteText = "Löschen"
Repository.I18N.TagInfo.none = "keine"
Repository.I18N.TagInfo.shared = "Gemeinsame Tags:"
Repository.I18N.TagInfo.newTag = "Neuer Tag"
Repository.I18N.TagInfo.addTag = "Hinzufügen"
		
if(!Repository.I18N.ModelRangeSelection) Repository.I18N.ModelRangeSelection = {};
Repository.I18N.ModelRangeSelection.previous = "<< Vorherige Seite"
Repository.I18N.ModelRangeSelection.next = "Nächste Seite >>"
Repository.I18N.ModelRangeSelection.last = "Letzte"
Repository.I18N.ModelRangeSelection.first = "Erste"
Repository.I18N.ModelRangeSelection.modelsOf = "(#{number} von #{size} Modellen)" 

if(!Repository.I18N.AccessInfo) Repository.I18N.AccessInfo = {};
Repository.I18N.AccessInfo.name = "Rechteverwaltung"
Repository.I18N.AccessInfo.publicText = "Öffentlich";
Repository.I18N.AccessInfo.notPublicText  = "Nicht öffentlich";
Repository.I18N.AccessInfo.noneIsSelected  = "Kein Model selektiert";
Repository.I18N.AccessInfo.none  = "kein";
Repository.I18N.AccessInfo.deleteText  = "Löschen";
Repository.I18N.AccessInfo.publish  = "Veröffentlichen";
Repository.I18N.AccessInfo.unPublish  = "Veröffenlichung zurückziehen";
Repository.I18N.AccessInfo.owner = "Besitzer:"
Repository.I18N.AccessInfo.contributer = "Schreibrechte:"
Repository.I18N.AccessInfo.reader = "Leserechte:"
Repository.I18N.AccessInfo.openid = "OpenID"
Repository.I18N.AccessInfo.addReader = "Leserechte hinzufügen"
Repository.I18N.AccessInfo.addContributer = "Schreibrechte hinzufügen"


if(!Repository.I18N.SortingSupport) Repository.I18N.SortingSupport = {};
Repository.I18N.SortingSupport.name = "Sortierung";
Repository.I18N.SortingSupport.lastchange = "Nach letzter Änderung"
Repository.I18N.SortingSupport.title = "Nach Titel"

if(!Repository.I18N.Export) Repository.I18N.Export = {};
Repository.I18N.Export.name = "Export";
Repository.I18N.Export.title = "Verfügbare Export Formate:"
Repository.I18N.Export.onlyOne = "Es darf nur ein Modell selektiert sein"

if(!Repository.I18N.UpdateButton) Repository.I18N.UpdateButton = {};
Repository.I18N.UpdateButton.name = "Aktualisieren"

if(!Repository.I18N.Edit) Repository.I18N.Edit = {};
Repository.I18N.Edit.name = "Editieren"
Repository.I18N.Edit.editSummary = "Beschreibung ändern"
Repository.I18N.Edit.editName = "Name ändern"
Repository.I18N.Edit.nameText = "Name"
Repository.I18N.Edit.summaryText = "Beschreibung"
Repository.I18N.Edit.editText = "Änderung speichern"
Repository.I18N.Edit.deleteText = "Löschen"

Repository.I18N.Edit.deleteOneText = "'#{title}'" 
Repository.I18N.Edit.deleteMoreText = "Alle #{size} selektierten Modelle" 