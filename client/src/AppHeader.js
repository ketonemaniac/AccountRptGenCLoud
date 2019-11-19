import React, { Component } from 'react';
import { Navbar, NavbarBrand, Nav, NavItem, NavLink } from 'reactstrap';
import './AppHeader.css';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faUserAlt } from '@fortawesome/free-solid-svg-icons';

class AppHeader extends Component {

  state = {
    info: {}
  }

  componentDidMount() {
    this.callApi()
      .then(res => this.setState({ info: res }))
      .catch(err => console.log(err));
  }

  callApi = async () => {
    const response = await fetch('/version');
    const body = await response.json();
    if (response.status !== 200) throw Error(body.message);
    return body;
  };


  render() {
    return (
      <Navbar>
        <NavbarBrand href="/">
          Account Report Generator <span><small className="text-sm font-weight-light">{this.state.info.version}</small></span>
        </NavbarBrand>
        <Nav>
          <NavItem>
            <div className="header-user" onClick={this.props.toggleUserModal}>
              <FontAwesomeIcon icon={faUserAlt} className="mr-2" />{this.state.info.user}
            </div>
          </NavItem>
        </Nav>
      </Navbar>
    );
  }
}

// <Nav>
// <NavItem>
//   <NavLink href="/" disabled={!this.props.isAdmin} 
//   className={this.props.isAdmin ? "text-primary" : "text-secondary"}
//   onClick={event => {
//     // override native behavior
//     event.preventDefault();
//     window.history.pushState(null, null, '/');
//     this.props.setAdmin(false);  
//   }
//   }>Home</NavLink>
// </NavItem>
// <NavItem>
//   <NavLink href="/admin" disabled={this.props.isAdmin} 
//   className={this.props.isAdmin ? "text-secondary" : "text-primary"}
//     onClick={event => {
//       // override native behavior
//       event.preventDefault();
//       window.history.pushState(null, null, 'admin');
//       this.props.setAdmin(true);  
//     }
//   }>Admin</NavLink>
// </NavItem>
// </Nav>

export default AppHeader;
