import FlatButton from 'material-ui/FlatButton';
import MenuItem from 'material-ui/MenuItem';
import SelectField from 'material-ui/SelectField';
import TextField from 'material-ui/TextField';
import * as React from 'react';
import 'UserEditForm.scss';
import {buttonStyle, floatingLabelFocusStyle, underlineFocusStyle} from '../../../app/themes';
import {Column} from '../../../components/layouts/column/Column';
import {firstUpperTranslated} from '../../../services/translationService';
import {Organisation, Role, User} from '../../../state/domain-models/user/userModels';
import {uuid} from '../../../types/Types';
import SelectFieldProps = __MaterialUI.SelectFieldProps;

interface UserFormProps {
  onSubmit: (event: any) => void;
  organisations: Organisation[];
  possibleRoles: Role[];
  isEditSelf: boolean;
  user?: User;
}

interface State {
  id?: uuid;
  name: string;
  email: string;
  organisation: Organisation;
  roles: Role[];
  password: string;
  repeatPassword: string;
}

export class UserEditForm extends React.Component<UserFormProps, State> {

  constructor(props: UserFormProps) {
    super(props);
    if (props.user) {
      this.state = {...props.user, password: '', repeatPassword: ''};
    } else {
      this.state = {
        name: '',
        email: '',
        organisation: {id: '', code: '', name: ''},
        roles: [Role.USER],
        password: '',
        repeatPassword: '',
      };
    }
  }

  changeOrganisation = (event, index, value) => this.setState({organisation: {id: value}});
  changeRoles = (event, index, value) => this.setState({roles: value});
  onChange = (event) => this.setState({[event.target.id]: event.target.value});
  onChangePassword = (event) => this.setState({password: event.target.value});
  onChangeRepeatPassword = (event) => this.setState({repeatPassword: event.target.value});

  render() {
    const {onSubmit, organisations, possibleRoles, isEditSelf} = this.props;
    const {name, email, organisation, roles, password, repeatPassword} = this.state;

    const nameLabel = firstUpperTranslated('name');
    const emailLabel = firstUpperTranslated('email');
    const organisationLabel = firstUpperTranslated('organisation');
    const rolesLabel = firstUpperTranslated('user roles');
    const newPasswordLabel = isEditSelf ?
      firstUpperTranslated('new password') : firstUpperTranslated('password');
    const repeatPasswordLabel = isEditSelf ?
      firstUpperTranslated('repeat new password') : firstUpperTranslated('repeat password');

    const wrappedSubmit = () => onSubmit(this.state);

    const organisationsAsItems = organisations.map((org, index) =>
      <MenuItem key={index} value={org.id} primaryText={org.name}/>);
    const rolesAsItems = possibleRoles.map((role, index) =>
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
            value={name}
            onChange={this.onChange}
          />
          <TextField
            className="TextField"
            floatingLabelText={emailLabel}
            floatingLabelFocusStyle={floatingLabelFocusStyle}
            hintText={emailLabel}
            underlineFocusStyle={underlineFocusStyle}
            id="email"
            value={email}
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
            value={organisation.id}
            disabled={isEditSelf}
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
            value={roles}
            disabled={isEditSelf}
          >
            {rolesAsItems}
          </WrappedSelectField>
          <TextField
            className="TextField"
            floatingLabelText={newPasswordLabel}
            floatingLabelFocusStyle={floatingLabelFocusStyle}
            hintText={newPasswordLabel}
            underlineFocusStyle={underlineFocusStyle}
            type="password"
            name="password"
            value={password}
            onChange={this.onChangePassword}
          />

          <TextField
            className="TextField"
            floatingLabelText={repeatPasswordLabel}
            floatingLabelFocusStyle={floatingLabelFocusStyle}
            hintText={repeatPasswordLabel}
            underlineFocusStyle={underlineFocusStyle}
            type="password"
            name="repeatPassword"
            value={repeatPassword}
            onChange={this.onChangeRepeatPassword}
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
