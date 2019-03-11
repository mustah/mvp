import {FlatButton} from 'material-ui';
import * as React from 'react';
import {ValidatorForm} from 'react-material-ui-form-validator';
import {buttonStyle} from '../../app/themes';
import {firstUpperTranslated, translate} from '../../services/translationService';
import {Password, User} from '../../state/domain-models/user/userModels';
import {uuid} from '../../types/Types';
import {ErrorMessage} from '../error-message/ErrorMessage';
import {ValidatedFieldInput} from '../inputs/ValidatedFieldInput';
import {Column} from '../layouts/column/Column';
import {Row} from '../layouts/row/Row';
import './PasswordEditForm.scss';

interface PasswordFormProps {
  onSubmit: (password: Password, userId: uuid) => void;
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
          <Row className="Error-message-container">
            <ErrorMessage message={error}/>
          </Row>
          <FlatButton
            type="submit"
            className="SaveButton"
            label={firstUpperTranslated('change password')}
            style={buttonStyle}
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
