import React, { Component } from 'react';
import { BrowserRouter as Router, Route, Redirect } from "react-router-dom";
import App from './home/App';
import UserAdmin from './useradmin/UserAdmin.js'
import Settings from './settings/Settings.js'
import Frame from './Frame.js';
import Login from './login/Login';


const Routing = () => {

    return (
            <Router>
                <Route exact path="/">
                    <Redirect to='/app/main' />
                </Route>
                <Route exact path="/login" component={Login} />
                <Route path="/app" component={Frame} />
                <Route path="/app/users" component={UserAdmin} />
                <Route path="/app/settings" component={Settings} />
                <Route path="/app/main" component={App} />
            </Router>
    );
}

export default Routing; 