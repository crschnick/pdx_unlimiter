

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


## Structure

A savegame consists out of many entries, where each entry can have a name,
also called the key, and an associated value.
If an entry has a key name, it is formatted as `<key name> = <value>` and look like this:

![Entry](docs/data-entry.png)

If an entry does not have a key, its index is displayed as its key:

![No-Key](docs/nokey.png)

### Primitives

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

A value can also be a list of any of the primitive types of the ones listed above and would look like this:

![List-Entry](docs/list-entry.png)

You can expand the list contents by clicking on it.
For any list, you can also use a preview feature to peek at its contents:
<p>
<img align="left" src="docs/preview.png" width=28/>
A button that you can hover over to get a preview of the list contents in text form.
</p>

### Objects

If entries of a list also have key names associated with them, they can be thought of as objects.
A special type descriptor is then used to distinguish them from normal lists:
<p>
<img align="left" src="docs/complex.png" width=28/>
Complex type, assigned if a list contains key-value pairs on its own, its type is described as complex
</p>

### Synthetic lists

It is also possible that there are multiple entries with the same key name.
In this case, they are automatically merged into a synthetic list entry:

![Synthetic-Entry](docs/synthetic.png)

They are called synthetic, because these entries do not actually exist like this in the savegame.
In the case shown above, there actually exist 732 entries with the key `rebel_faction` instead.
This merging is done for readability.
