import * as React from 'react';
import {ValidatorForm} from 'react-material-ui-form-validator';
import {Overwrite} from 'utility-types';
import {firstUpperTranslated} from '../../services/translationService';
import {Organisation} from '../../state/domain-models/organisation/organisationModels';
import {Role, User} from '../../state/domain-models/user/userModels';
import {Language} from '../../state/language/languageModels';
import {IdNamed, OnClickEventHandler, uuid} from '../../types/Types';
import {ButtonSave} from '../buttons/ButtonSave';
import {ValidatedFieldInput} from '../inputs/ValidatedFieldInput';
import {ValidatedInputSelectable} from '../inputs/ValidatedInputSelectable';
import {Column} from '../layouts/column/Column';
import './UserEditForm.scss';

interface UserFormProps {
  onSubmit: OnClickEventHandler;
  organisations: Organisation[];
  possibleRoles: Role[];
  languages: Language[];
  isEditSelf: boolean;
  user?: User;
}

export type State = Overwrite<User, {id?: uuid; password: string}>;

const requiredValidator: string[] = ['required'];
const requiredEmailValidator: string[] = ['required', 'isEmail'];

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
        roles: [Role.MVP_USER],
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
    const requiredLabel = firstUpperTranslated('required field');
    const requiredEmailLabel = firstUpperTranslated('email not valid');

    const roleOptions: IdNamed[] = possibleRoles.map((role) => ({id: role, name: role.toString()}));
    const languageOptions: IdNamed[] = languages.map(({code, name}) => ({id: code, name}));

    const requiredMessage = [requiredLabel];
    const requiredEmailMessage = [requiredLabel, requiredEmailLabel];

    const passwordElement = user ? null : (
      <ValidatedFieldInput
        id="password"
        floatingLabelText={newPasswordLabel}
        hintText={newPasswordLabel}
        type="password"
        value={password}
        validators={requiredValidator}
        errorMessages={requiredMessage}
        autoComplete="new-password"
        onChange={this.onChangePassword}
      />
    );

    return (
      <ValidatorForm
        onSubmit={this.wrappedSubmit}
      >
        <Column className="EditUserContainer">
          <ValidatedFieldInput
            floatingLabelText={nameLabel}
            hintText={nameLabel}
            id="name"
            value={name}
            validators={requiredValidator}
            errorMessages={requiredMessage}
            autoComplete="new-password"
            onChange={this.onChangeName}
          />
          <ValidatedFieldInput
            floatingLabelText={emailLabel}
            hintText={emailLabel}
            id="email"
            value={email}
            validators={requiredEmailValidator}
            errorMessages={requiredEmailMessage}
            autoComplete="new-password"
            onChange={this.onChangeEmail}
          />

          <ValidatedInputSelectable
            options={organisations}
            floatingLabelText={organisationLabel}
            hintText={organisationLabel}
            id="organisation"
            multiple={false}
            onChange={this.changeOrganisation}
            validators={requiredValidator}
            errorMessages={requiredMessage}
            value={organisation.id}
            disabled={isEditSelf}
          />
          <ValidatedInputSelectable
            options={roleOptions}
            floatingLabelText={rolesLabel}
            hintText={rolesLabel}
            id="roles"
            multiple={true}
            onChange={this.changeRoles}
            validators={requiredValidator}
            errorMessages={requiredMessage}
            value={roles}
            disabled={isEditSelf}
          />

          <ValidatedInputSelectable
            options={languageOptions}
            floatingLabelText={languageLabel}
            hintText={languageLabel}
            id="language"
            multiple={false}
            onChange={this.changeLanguage}
            validators={requiredValidator}
            errorMessages={requiredMessage}
            value={language}
          />

          {passwordElement}

          <ButtonSave className="ButtonSave" type="submit"/>
        </Column>
      </ValidatorForm>
    );
  }

  changeOrganisation = (_, __, value) =>
    this.setState({
      organisation: this.props.organisations.find(({id}) => id === value)!
    })

  changeRoles = (_, __, value) => this.setState({roles: value});

  changeLanguage = (_, __, value) => this.setState({language: value});

  onChangePassword = (event) => this.setState({password: event.target.value});

  onChangeName = (event) => this.setState({name: event.target.value});

  onChangeEmail = (event) => this.setState({email: event.target.value});

  wrappedSubmit = (event) => {
    event.preventDefault();
    this.props.onSubmit(this.state);
  }
}
