# Poedit

*Keywords: i18n, translation*

We keep translations in .po files. They can be edited with the program *Poedit*, available for at least Ubuntu and macOS.

Generally, all keys are written in English. If you copy the value from the translated term, you are fine.

To assist you when translating to Swedish, you can install a package that gives you pretty accurate hints. On Ubuntu, install the package *aspell-sv*.

## Workflow

After having installed the *Poedit* program:

1. Add a string that needs to be translated, in a .tsx file: `<div>{translate('hello')}</div>`
1. Extract the .pot file from the application: `yarn start` in *frontend/*
1. Edit all .po files: `Poedit src/i18n/locales/*po`
1. In Poedit's menu: *Catalog > Update from POT file*, select *template.pot* inside *frontend/src/i18n/locales*
1. New fields are added to the file, translate them! If you have *aspell-sv* installed, it's as easy as:
    1. Select a line that is not yet translated
    1. Look at the right hand side menu
    1. Pick one of the choices with `ctrl+1` or `ctrl+2`, etc
1. Save the file
1. Commit the .po files
