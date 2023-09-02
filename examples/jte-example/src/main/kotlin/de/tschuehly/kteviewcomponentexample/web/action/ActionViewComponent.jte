@param de.tschuehly.kteviewcomponentexample.web.action.ActionViewComponent.Person person
@param java.util.HashMap<Integer,String> itemList
@param Integer counter
<html>
<head>
    <script src="http://localhost:35729/livereload.js"></script>
    <script defer src="/webjars/htmx.org/dist/htmx.min.js"></script>
</head>
<body>
<h2>ViewAction Get CountUp</h2>

<button view:action="countUp">Default ViewAction [GET]</button>
<h3>${counter}</h3>

<h2>ViewAction Post AddItem</h2>
<form view:action="addItem">
    <input type="text" name="item">
    <button type="submit">Save Item</button>
</form>
<table>
    <tr>
        <th>Item</th>
        <th>Action</th>
    </tr>
    @for(var item: itemList.entrySet())
        <tr>
            <td>
                <span>${item.getValue()}</span>
            </td>
            <td>
                <button view:action="deleteItem?id=${item.getKey()}">Delete Item ${item.getKey()}</button>
            </td>
        </tr>
    @endfor
</table>

<h2>ViewAction Put/Patch Person Form</h2>

<form style="display: inline-grid; gap: 0.5rem">
    <label>
        Name <input type="text" id="name" name="name" value="${person.getName()}">
    </label>
    <label>
        Age: <input type="number" id="age" name="age" value="${person.getAge()}">
    </label>
    <label>
        Location: <input type="text" id="location" name="location" value="${person.getLocation()}">
    </label>
    <button type="submit" view:action="savePersonPut">Save Changes using Put</button>
    <button type="submit" view:action="savePersonPatch">Save Changes using Patch</button>
</form>
</body>
</html>