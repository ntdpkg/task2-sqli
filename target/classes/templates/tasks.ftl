<!DOCTYPE html>
<html>
<head>
    <title>Todo List</title>

    <script>
        const token = localStorage.getItem('jwt_token');
        if (!token) {
            alert('You are not logged in. Redirecting to login page.');
            window.location.replace('/login'); 
        }
    </script>

    <style>
        body { font-family: sans-serif; max-width: 800px; margin: 50px auto; padding: 20px; }
        .container { border: 1px solid #ccc; padding: 20px; border-radius: 5px; margin-bottom: 20px;}
        input[type="text"] { width: 70%; padding: 8px; }
        input[type="submit"], button { padding: 8px 15px; cursor: pointer; }
        #logoutBtn { float: right; background-color: #dc3545; color: white; border: none; border-radius: 3px; }
        ul { list-style-type: none; padding: 0; }
        li { background: #f4f4f4; margin: 5px 0; padding: 10px; border-left: 5px solid #007bff; }
        hr { margin: 20px 0; }
        .import-results { background: #e9f7ef; border: 1px solid #b7e4c7; padding: 15px; margin-top: 20px; }
    </style>
</head>
<body>
    <button id="logoutBtn" onclick="logout(event)">Logout</button>
    <h1>Welcome, ${username}!</h1>
    <p><a href="/profile">View Your Profile</a></p> 

    
    <div class="container">
        <h3>Search Your Tasks</h3>
        <form action="/tasks" method="get">
            <input type="text" name="q" placeholder="Search tasks...">
            <input type="submit" value="Search">
            <h3>Sort by ID</h3>
            <select name="sort" onchange="document.getElementById('sortForm').submit();">
                <option value="ASC">Ascending</option>
                <option value="DESC">Descending</option>
            </select>
        </form>
    </div>

    <div class="container">
        <#if searchResults??>
            ${searchResults} 
        </#if>

        ${taskResults} 
    </div>
    
    <div class="container">
        <h3>Add New Task</h3>
        <form action="/tasks" method="post">
            <input type="text" name="title" placeholder="Task title..." required style="width: 100%; margin-bottom: 10px;">
            <input type="text" name="description" placeholder="Task description..." required style="width: 100%; margin-bottom: 10px;">
            <input type="submit" value="Add Task">
        </form>
    </div>

    <div class="container">
        <h3>Import Tasks from XML</h3>
        <form id="importTasks">
            <input type="file" name="xmlfile" required>
            <input type="submit" value="Import from XML" onclick="handleImportTasks(event)">
        </form>
        <div id="importMessage"></div>
    </div>
    
    <script src="main.js"></script>
</body>
</html>