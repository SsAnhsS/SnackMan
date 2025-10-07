import os
import json

def set_nested_value(dictionary, keys, value):
    '''
    Hilfsfunktion, um ein verschachteltes Key-Value-Paar in einem Dictionary zu setzen:
    
    dictionary = dict, in dem verschachteltes Paar gesetzt werden soll
    keys = Liste von (verschachtelten) keys
    value = Übersetzung
    
    1.   Iteriert über alle keys außer dem letzten, da dort die Übersetzung gesetzt wird
    2.   setdefault() überprüft ob key bereits in dict vorkommt, wenn nicht wird key mit value="{}" gesetzt
         -> returned inneres dict, somit ist dictionary nun das innere (eben erstellte) dict
    3.   letzter key wird mit Übersetzung gesetzt
    '''
    
    for key in keys[:-1]:
        dictionary = dictionary.setdefault(key, {}) 
    dictionary[keys[-1]] = value


def process_translation_entries(translation_entries, languages, language_dicts):
    '''
    Durchläuft jede Zeile mit Übersetzungen:
        1. entfernt Whitespace & Kommentarzeilen
        2. zerlegt Zeile in key und values
        3. zerlegt den key für verschachtelte Ebenen
        4. zip() kombiniert Elemente aus languages & values mit gleichem Index in einem Tupel
            - alle Tupel werden durchlaufen und in Variablen language & value aufgeteilt
            - das Key-Value-Paar wird in set_nested_value() formatiert und dem Dict der entsprechenden Sprache angehängt
    '''
    
    for line in translation_entries:
        # 1.
        line = line.replace("\n", "").strip()
        if line == "" or line.startswith("#"):
            continue

        # 2.
        line_attr = line.split(";")
        key = line_attr[0]
        values = line_attr[1:] # translations

        # 3.
        keys = key.split(".")
        
        # 4.
        for language, value in zip(languages, values):
            set_nested_value(language_dicts[language], keys, value)



def write_json_file(language, language_dict):
    '''
    Erstellt / überschreibt .json-File und formatiert diese
    '''
    
    with open(f"./frontend/src/assets/locales/{language}.json", "w") as json_file:
        json.dump(language_dict, json_file, ensure_ascii=False, indent=4)


def main():
    '''
    1. Erstellt den Filepath mit einem Workaround, direktes referenzieren ging nicht
    2. Datei einlesen
    3. Sprachen aus Header Zeile extrahieren
    4. leere Dicts für jede Sprache erstellen und in einem großen Dict speichern
        --> jede Sprache kann nur 1x hinterlegt sein
    5. Zeilen mit Übersetzungen verarbeiten und die Dicts der Sprachen füllen
    6. json-Dateien anhand der Dicts für jede Sprache erstellen
    '''
    
    # 1.
    script_dir = os.path.dirname(os.path.abspath(__file__))
    file_path = os.path.join(script_dir, "translations.csv")

    # 2.
    with open(file_path, "r") as translations:
        content = translations.readlines()

    # 3.
    header_line_attr = content[0].replace("\n", "").split(";")
    languages = header_line_attr[1:]

    # 4.
    language_dict = {}
    for language in languages:
        language_dict[language] = {}

    # 5.
    translation_entries = content[1:]
    process_translation_entries(translation_entries, languages, language_dict)

    # 6.
    for language in language_dict:
        write_json_file(language, language_dict[language])



main()