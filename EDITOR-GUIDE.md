
## Structure

A savegame consists out of many entries, where each entry has a name,
also called the key, and an associated value.
They are formatted as `<key name> = <value>` pairs and look like this:

![Entry](docs/data-entry.png)

The associated values can be of one of the following primitive types:
<p>
<img align="left" src="docs/boolean.png" width=28/>
Boolean, either <i>yes</i> or <i>no</i>
</p>
<p>
<img align="left" src="docs/integer.png" width=28/>
Integer, e.g. <i>124</i>
</p>
<p>
<img align="left" src="docs/float.png" width=28/>
Floating point number, e.g. <i>1.12</i>
</p>
<p>
<img align="left" src="docs/text.png" width=28/>
Text, e.g. <i>"Text value"</i>
</p>
<p>
<img align="left" src="docs/game-value.png" width=28/>
A game specific value, e.g. the relgion id <i>catholic</i> in eu4.
While it looks like a normal Text value, the difference is
that you can basically assign anything to a text value while
you can only assign certain things to a game specific value without breaking the game
</p>
<p>
<img align="left" src="docs/color.png" width=28/>
Color
</p>

### Lists

A value can also be a list of any type of the ones listed above and would look like this:

![List-Entry](docs/list-entry.png)

You can open the list contents by clicking on it.

<p>
<img align="left" src="docs/complex.png" width=28/>
Complex type, assigned if a list contains key-value pairs on its own, its type is described as complex
</p>

<p>
<img align="left" src="docs/preview.png" width=28/>
A button that you can hover over to get a preview of the list contents in text form.
</p>

## aaa



## Navigation

The navigation bar located at the top shows you exactly
where you currently are in your savegame and also allows you go back up.
It looks like this:

![Nav-Bar](docs/nav-bar.png)



## Filters

Since savegames contain thousands of entries, it is basically required
to use filters in order to find entries that you are looking for.
The filter bar is located at the bottom and looks like this:

![Filter](docs/filter-bar.png)

The search string can be entered into the text field.
To apply a search string, you have to press **Enter**.
On the right, the **Showing results for ...** label shows the currently active filter
The various buttons on the bar have the following usages:

<p>
<img align="left" src="docs/key.png" width=35/>
A toggle button to include keys in the search.
Key names are located to the left of the equal sign of an entry.
</p>

<p>
<img align="left" src="docs/value.png" width=35/>
A toggle button to include values in the search.
The values are the objects located to the right of the equal sign of an entry.
</p>

<p>
<img align="left" src="docs/filter.png" width=28/>
Applies the filter string in the text field.
Alternatively, you can press **Enter** after typing your search string.
</p>

<p>
<img align="left" src="docs/clear.png" width=28/>
Clears the active filter.
</p>

<p>
<img align="left" src="docs/case.png" width=35/>
Toggles case-sensitive matching mode for the search string.
</p>