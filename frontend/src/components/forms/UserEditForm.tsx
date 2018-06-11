import * as React from 'react';
import {Overwrite} from 'react-redux-typescript';
import {firstUpperTranslated} from '../../services/translationService';
import {Organisation} from '../../state/domain-models/organisation/organisationModels';
import {Role, User} from '../../state/domain-models/user/userModels';
import {Language} from '../../state/language/languageModels';
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
  languages: Language[];
  isEditSelf: boolean;
  user?: User;
}

type State = Overwrite<User, {id?: uuid; password: string}>;

export class UserEditForm extends React.Component<UserFormProps, State> {

  constructor(props: UserFormProps) {
    super(props);
    if (props.user) {
      this.state = {...props.user, password: ''};
    } else {
      this.state = {
        name: '',
        email: '',
        organisation: {id: '', slug: '', name: ''},
        roles: [Role.USER],
        language: 'en',
        password: '',
      };
    }
  }

  componentWillReceiveProps({user}: UserFormProps) {
    if (user && user !== this.props.user) {
      this.setState({...user, password: ''});
    }
  }

  shouldComponentUpdate(
    {user: nextUser, organisations: nextOrganisations}: UserFormProps,
    nextState: State,
  ) {
    const {user, organisations} = this.props;
    return user !== nextUser || organisations !== nextOrganisations || nextState !== this.state;
  }

  render() {
    const {organisations, possibleRoles, languages, isEditSelf, user} = this.props;
    const {name, email, organisation, roles, password, language} = this.state;

    const nameLabel = firstUpperTranslated('name');
    const emailLabel = firstUpperTranslated('email');
    const organisationLabel = firstUpperTranslated('organisation');
    const rolesLabel = firstUpperTranslated('user roles');
    const languageLabel = firstUpperTranslated('user language');
    const newPasswordLabel = isEditSelf ?
      firstUpperTranslated('new password') : firstUpperTranslated('password');

    const roleOptions: IdNamed[] = possibleRoles.map((role) => ({id: role, name: role.toString()}));
    const languageOptions: IdNamed[] = languages.map(({code, name}) => ({id: code, name}));

    const passwordElement = user ? null : (
      <TextFieldInput
        id="password"
        floatingLabelText={newPasswordLabel}
        hintText={newPasswordLabel}
        type="password"
        value={password}
        onChange={this.onChange}
      />
    );

    return (
      <form onSubmit={this.wrappedSubmit}>
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
            options={organisations}
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
          <SelectFieldInput
            options={languageOptions}
            floatingLabelText={languageLabel}
            hintText={languageLabel}
            id="language"
            onChange={this.changeLanguage}
            value={language}
          />
          {passwordElement}
          <ButtonSave
            className="SaveButton"
            type="submit"
          />
        </Column>
      </form>
    );
  }

  changeOrganisation = (event, index, value) =>
    this.setState({organisation: this.organisationById(value)})

  changeRoles = (event, index, value) => this.setState({roles: value});

  changeLanguage = (event, index, value) => this.setState({language: value});

  onChange = (event) => this.setState({[event.target.id]: event.target.value});

  organisationById = (orgId: uuid): Organisation =>
    this.props.organisations.find(({id}) => id === orgId)!

  wrappedSubmit = (event) => {
    event.preventDefault();
    this.props.onSubmit(this.state);
  }
}
