import * as React from 'react';
import {firstUpperTranslated} from '../../services/translationService';
import {Organisation, Role, User} from '../../state/domain-models/user/userModels';
import {IdNamed, uuid} from '../../types/Types';
import {ButtonSave} from '../buttons/ButtonSave';
import {SelectFieldInput} from '../inputs/InputSelectable';
import {TextFieldInput} from '../inputs/InputText';
import {Column} from '../layouts/column/Column';
import './UserEditForm.scss';

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
}

export class UserEditForm extends React.Component<UserFormProps, State> {

  constructor(props: UserFormProps) {
    super(props);
    if (props.user) {
      this.state = {...props.user, password: ''};
    } else {
      this.state = {
        name: '',
        email: '',
        organisation: {id: '', code: '', name: ''},
        roles: [Role.USER],
        password: '',
      };
    }
  }

  organisationById = (orgId: uuid): Organisation => this.props.organisations.filter(({id}) => id === orgId)[0];
  changeOrganisation = (event, index, value) => this.setState({organisation: this.organisationById(value)});
  changeRoles = (event, index, value) => this.setState({roles: value});
  onChange = (event) => this.setState({[event.target.id]: event.target.value});

  render() {
    const {onSubmit, organisations, possibleRoles, isEditSelf} = this.props;
    const {name, email, organisation, roles, password} = this.state;

    const nameLabel = firstUpperTranslated('name');
    const emailLabel = firstUpperTranslated('email');
    const organisationLabel = firstUpperTranslated('organisation');
    const rolesLabel = firstUpperTranslated('user roles');
    const newPasswordLabel = isEditSelf ?
      firstUpperTranslated('new password') : firstUpperTranslated('password');

    const wrappedSubmit = (event) => {
      event.preventDefault();
      onSubmit(this.state);
    };

    const organisationOptions: IdNamed[] = organisations.map(({id, name}: Organisation) => ({id, name}));
    const roleOptions: IdNamed[] = possibleRoles.map((role) => ({id: role, name: role.toString()}));

    return (
      <form onSubmit={wrappedSubmit}>
        <Column className="EditUserContainer">
          <TextFieldInput
            floatingLabelText={nameLabel}
            hintText={nameLabel}
            id="name"
            value={name}
            onChange={this.onChange}
          />
          <TextFieldInput
            floatingLabelText={emailLabel}
            hintText={emailLabel}
            id="email"
            value={email}
            onChange={this.onChange}
          />
          <SelectFieldInput
            options={organisationOptions}
            floatingLabelText={organisationLabel}
            hintText={organisationLabel}
            id="organisation"
            onChange={this.changeOrganisation}
            value={organisation.id}
            disabled={isEditSelf}
          />
          <SelectFieldInput
            options={roleOptions}
            floatingLabelText={rolesLabel}
            hintText={rolesLabel}
            id="roles"
            multiple={true}
            onChange={this.changeRoles}
            value={roles}
            disabled={isEditSelf}
          />
          <TextFieldInput
            id="password"
            floatingLabelText={newPasswordLabel}
            hintText={newPasswordLabel}
            type="password"
            value={password}
            onChange={this.onChange}
          />
          <ButtonSave
            className="SaveButton"
            type="submit"
          />
        </Column>
      </form>
    );
  }
}
