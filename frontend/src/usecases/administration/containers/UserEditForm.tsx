import FlatButton from 'material-ui/FlatButton';
import MenuItem from 'material-ui/MenuItem';
import SelectField from 'material-ui/SelectField';
import TextField from 'material-ui/TextField';
import * as React from 'react';
import {buttonStyle, floatingLabelFocusStyle, underlineFocusStyle} from '../../../app/themes';
import {Column} from '../../../components/layouts/column/Column';
import {firstUpperTranslated} from '../../../services/translationService';
import {Organisation, Role, User} from '../../../state/domain-models/user/userModels';
import {AdministrationState} from '../administrationModels';
import 'UserEditForm.scss';
import SelectFieldProps = __MaterialUI.SelectFieldProps;

interface UserFormProps {
  onSubmit: (event: any) => void;
  organisations: Organisation[];
  roles: Role[];
  currentUserRoles: Role[];
  user?: User;
}

export class UserEditForm extends React.Component<UserFormProps & AdministrationState> {

  state = {
    organisation: {id: ''},
    roles: [],
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
        <Column className="EditUserContainer">
          <TextField
            className="TextField"
            floatingLabelText={nameLabel}
            floatingLabelFocusStyle={floatingLabelFocusStyle}
            hintText={nameLabel}
            underlineFocusStyle={underlineFocusStyle}
            id="name"
            onChange={this.onChange}
          />
          <TextField
            className="TextField"
            floatingLabelText={emailLabel}
            floatingLabelFocusStyle={floatingLabelFocusStyle}
            hintText={emailLabel}
            underlineFocusStyle={underlineFocusStyle}
            id="email"
            onChange={this.onChange}
          />
          <WrappedSelectField
            className="SelectField"
            floatingLabelText={organisationLabel}
            floatingLabelFocusStyle={floatingLabelFocusStyle}
            hintText={organisationLabel}
            underlineFocusStyle={underlineFocusStyle}
            id="organisation"
            onChange={this.changeOrganisation}
            value={this.state.organisation.id}
          >
            {organisationsAsItems}
          </WrappedSelectField>
          <WrappedSelectField
            className="SelectField"
            floatingLabelText={rolesLabel}
            floatingLabelFocusStyle={floatingLabelFocusStyle}
            hintText={rolesLabel}
            underlineFocusStyle={underlineFocusStyle}
            id="roles"
            multiple={true}
            onChange={this.changeRoles}
            value={this.state.roles}
          >
            {rolesAsItems}
          </WrappedSelectField>
          <TextField
            className="TextField"
            floatingLabelText={firstUpperTranslated('new password')}
            floatingLabelFocusStyle={floatingLabelFocusStyle}
            hintText={firstUpperTranslated('new password')}
            underlineFocusStyle={underlineFocusStyle}
            type="password"
            name="newPassword"
          />

          <TextField
            className="TextField"
            floatingLabelText={firstUpperTranslated('repeat new password')}
            floatingLabelFocusStyle={floatingLabelFocusStyle}
            hintText={firstUpperTranslated('repeat new password')}
            underlineFocusStyle={underlineFocusStyle}
            type="password"
            name="repeatPassword"
          />
          <FlatButton
            className="SaveButton"
            type="submit"
            label={firstUpperTranslated('save')}
            style={buttonStyle}
          />
        </Column>
      </form>
    );
  }
}

/*
TODO: WrappedSelectField is used as a workaround until @types/material-ui/SelectField is
supporting floatingLabelFocusStyle.
*/
interface WrappedSelectFieldProps extends SelectFieldProps {
  floatingLabelFocusStyle?: React.CSSProperties;
}

const WrappedSelectField = (props: WrappedSelectFieldProps) => <SelectField {...props} />;
