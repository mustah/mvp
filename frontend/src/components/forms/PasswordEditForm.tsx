import {FlatButton} from 'material-ui';
import * as React from 'react';
import {buttonStyle} from '../../app/themes';
import {firstUpperTranslated, translate} from '../../services/translationService';
import {Password, User} from '../../state/domain-models/user/userModels';
import {uuid} from '../../types/Types';
import {ErrorMessage} from '../error-message/ErrorMessage';
import {TextFieldInput} from '../inputs/TextFieldInput';
import {Column} from '../layouts/column/Column';
import './PasswordEditForm.scss';
import {Row} from '../layouts/row/Row';

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

    return (
      <form onSubmit={this.wrappedSubmit}>
        <Column>
          <TextFieldInput
            id="password"
            floatingLabelText={newPasswordLabel}
            hintText={newPasswordLabel}
            type="password"
            value={password}
            onChange={this.onChange}
          />
          <TextFieldInput
            id="passwordConfirm"
            floatingLabelText={confirmPasswordLabel}
            hintText={confirmPasswordLabel}
            type="password"
            value={passwordConfirm}
            onChange={this.onChange}
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
      </form>
    );
  }

  onChange = (event) => this.setState({[event.target.id]: event.target.value, error: ''});

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
