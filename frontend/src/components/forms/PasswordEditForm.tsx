import * as React from 'react';
import {ValidatorForm} from 'react-material-ui-form-validator';
import {firstUpperTranslated, translate} from '../../services/translationService';
import {Password, User} from '../../state/domain-models/user/userModels';
import {uuid} from '../../types/Types';
import {ButtonPrimary} from '../buttons/ButtonPrimary';
import {ErrorMessage} from '../error-message/ErrorMessage';
import {ValidatedFieldInput} from '../inputs/ValidatedFieldInput';
import {Column} from '../layouts/column/Column';
import {Row} from '../layouts/row/Row';

export type OnChangePassword = (password: Password, userId: uuid) => void;

interface PasswordFormProps {
  onSubmit: OnChangePassword;
  user: User;
}

interface PasswordEditFormState {
  password: string;
  passwordConfirm: string;
  error: string;
  userId: uuid;
}

const requiredValidator: string[] = ['required'];

type State = PasswordEditFormState;

export class PasswordEditForm extends React.Component<PasswordFormProps, State> {

  constructor(props: PasswordFormProps) {
    super(props);

    this.state = {
      password: '',
      passwordConfirm: '',
      error: '',
      userId: props.user.id,
    };
  }

  render() {
    const {password, passwordConfirm, error} = this.state;

    const newPasswordLabel = firstUpperTranslated('new password');
    const confirmPasswordLabel = firstUpperTranslated('confirm new password');

    const requiredMessage = [firstUpperTranslated('required field')];

    return (
      <ValidatorForm onSubmit={this.wrappedSubmit}>
        <Column>
          <ValidatedFieldInput
            id="password"
            floatingLabelText={newPasswordLabel}
            hintText={newPasswordLabel}
            type="password"
            value={password}
            validators={requiredValidator}
            errorMessages={requiredMessage}
            onChange={this.onChangePassword}
          />
          <ValidatedFieldInput
            id="passwordConfirm"
            floatingLabelText={confirmPasswordLabel}
            hintText={confirmPasswordLabel}
            type="password"
            value={passwordConfirm}
            validators={requiredValidator}
            errorMessages={requiredMessage}
            onChange={this.onChangePasswordConfirm}
          />
          <Row style={{height: 30}}>
            <ErrorMessage message={error} style={{margin: 0}}/>
          </Row>
          <ButtonPrimary
            type="submit"
            className="flex-align-self-start"
            label={firstUpperTranslated('change password')}
          />
        </Column>
      </ValidatorForm>
    );
  }

  onChangePassword = (event) => this.setState({password: event.target.value, error: ''});

  onChangePasswordConfirm = (event) => this.setState({passwordConfirm: event.target.value, error: ''});

  wrappedSubmit = (event) => {
    event.preventDefault();

    const {password, passwordConfirm, userId} = this.state;

    if (password === passwordConfirm && password.length) {
      this.props.onSubmit(this.state, userId);
    } else {
      this.setState({error: translate('passwords don\'t match')});
    }
  }
}
