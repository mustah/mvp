import MenuItem from 'material-ui/MenuItem';
import RaisedButton from 'material-ui/RaisedButton';
import SelectField from 'material-ui/SelectField';
import TextField from 'material-ui/TextField';
import * as React from 'react';
import {Row} from '../../../components/layouts/row/Row';
import {firstUpperTranslated} from '../../../services/translationService';
import {Organisation, Role, User} from '../../../state/domain-models/user/userModels';
import {AdministrationState} from '../administrationModels';

interface UserFormProps {
  onSubmit: (event: any) => void;
  organisations: Organisation[];
  roles: Role[];
  user?: User;
}

class UserEdit extends React.Component<UserFormProps & AdministrationState> {

  state = {
    organisation: {id: ''},
    roles: [Role.USER],
  };

  changeOrganisation = (event, index, value) => this.setState({organisation: {id: value}});

  changeRoles = (event, index, value) => this.setState({roles: value});

  onChange = (event) => this.setState({[event.target.id]: event.target.value});

  render() {
    const {onSubmit, organisations, roles} = this.props;

    const nameLabel = firstUpperTranslated('name');
    const emailLabel = firstUpperTranslated('email');
    const organisationLabel = firstUpperTranslated('organisation');
    const rolesLabel = firstUpperTranslated('user roles');

    const wrappedSubmit = () => onSubmit(this.state);

    const organisationsAsItems = organisations.map((org, index) =>
      <MenuItem key={index} value={org.id} primaryText={org.name}/>);
    const rolesAsItems = roles.map((role, index) =>
      <MenuItem key={index} value={role} primaryText={role.toString()}/>);

    return (
      <form onSubmit={wrappedSubmit}>
        <Row>
          <TextField
            floatingLabelText={nameLabel}
            hintText={nameLabel}
            id="name"
            onChange={this.onChange}
          />
        </Row>
        <Row>
          <TextField
            floatingLabelText={emailLabel}
            hintText={emailLabel}
            id="email"
            onChange={this.onChange}
          />
        </Row>
        <Row>
          <SelectField
            floatingLabelText={organisationLabel}
            hintText={organisationLabel}
            id="organisation"
            onChange={this.changeOrganisation}
            value={this.state.organisation.id}
          >
            {organisationsAsItems}
          </SelectField>
        </Row>
        <Row>
          <SelectField
            floatingLabelText={rolesLabel}
            hintText={rolesLabel}
            id="roles"
            multiple={true}
            onChange={this.changeRoles}
            value={this.state.roles}
          >
            {rolesAsItems}
          </SelectField>
        </Row>
        <Row>
          <RaisedButton
            label={firstUpperTranslated('save')}
            onClick={wrappedSubmit}
          />
        </Row>
      </form>
    );
  }
}

export const UserEditForm = UserEdit;
