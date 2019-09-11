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

interface UserFormProps {
  onSubmit: OnClickEventHandler;
  organisations: Organisation[];
  possibleRoles: Role[];
  languages: Language[];
  isEditSelf: boolean;
  user?: User;
}

export type State = Overwrite<User, {id?: uuid; password: string}>;

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
        roles: [],
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

    const newPasswordLabel = isEditSelf ? firstUpperTranslated('new password') : firstUpperTranslated('password');
    const roleOptions: IdNamed[] = possibleRoles.map((role) => ({id: role, name: role.toString()}));
    const languageOptions: IdNamed[] = languages.map(({code, name}) => ({id: code, name}));

    const requiredEmailValidator: string[] = ['required', 'isEmail'];
    const requiredEmailMessage = [(firstUpperTranslated('required field')), firstUpperTranslated('email not valid')];

    const passwordElement = user ? null : (
      <ValidatedFieldInput
        id="password"
        labelText={newPasswordLabel}
        type="password"
        value={password}
        autoComplete="new-password"
        onChange={this.onChangePassword}
      />
    );

    return (
      <ValidatorForm onSubmit={this.wrappedSubmit}>
        <Column>
          <ValidatedFieldInput
            autoComplete="new-password"
            id="name"
            labelText={firstUpperTranslated('name')}
            onChange={this.onChangeName}
            value={name}
          />

          <ValidatedFieldInput
            autoComplete="new-password"
            id="email"
            labelText={firstUpperTranslated('email')}
            onChange={this.onChangeEmail}
            value={email}
            validators={requiredEmailValidator}
            errorMessages={requiredEmailMessage}
          />

          <ValidatedInputSelectable
            disabled={isEditSelf}
            id="organisation"
            labelText={firstUpperTranslated('organisation')}
            multiple={false}
            options={organisations}
            onChange={this.changeOrganisation}
            value={organisation.id}
          />

          <ValidatedInputSelectable
            disabled={isEditSelf}
            id="roles"
            labelText={firstUpperTranslated('user roles')}
            multiple={true}
            onChange={this.changeRoles}
            options={roleOptions}
            value={roles}
          />

          <ValidatedInputSelectable
            id="language"
            labelText={firstUpperTranslated('user language')}
            multiple={false}
            onChange={this.changeLanguage}
            options={languageOptions}
            value={language}
          />

          {passwordElement}

          <ButtonSave className="flex-align-self-start" type="submit"/>
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
