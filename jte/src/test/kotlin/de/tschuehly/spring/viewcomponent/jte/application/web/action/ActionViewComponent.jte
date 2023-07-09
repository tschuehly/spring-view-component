@param de.tschuehly.spring.viewcomponent.jte.application.web.action.ActionViewComponent.Person person
@param java.util.HashMap<String,String> itemList
@param Integer counter

<html>
<head>
    <script src="https://unpkg.com/htmx.org@1.9.2/dist/htmx.js"></script>
</head>
<body>

<h2>ViewAction Get CountUp</h2>

<button view:action="countUp">Default ViewAction [GET]</button>
<h3>${counter}</h3>


<h2>ViewAction Post AddItem</h2>
<form view:action="addItem">
    <label>
        Item:
    </label>
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
                <button view:action="deleteItem?id=${item.getKey()}">Delete Item ${item.getKey()}</button>
            </td>

        </tr>
    @endfor
</table>
</body>
</html>