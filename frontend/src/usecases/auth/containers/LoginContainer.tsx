import * as classNames from 'classnames';
import {Paper} from 'material-ui';
import * as React from 'react';
import {connect} from 'react-redux';
import {RouteComponentProps} from 'react-router';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {getLogoPath} from '../../../app/routes';
import {ButtonLogin} from '../../../components/buttons/ButtonLogin';
import {ErrorMessage} from '../../../components/error-message/ErrorMessage';
import {TextFieldInput} from '../../../components/inputs/InputText';
import {ColumnCenter} from '../../../components/layouts/column/Column';
import {RowCenter} from '../../../components/layouts/row/Row';
import {Logo} from '../../../components/logo/Logo';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated} from '../../../services/translationService';
import {login} from '../authActions';
import {AuthState} from '../authModels';
import './LoginContainer.scss';

interface StateToProps {
  auth: AuthState;
}

interface DispatchToProps {
  login: (email: string, password: string) => void;
}

interface LoginState {
  email: string;
  password: string;
}

type Props =
  & StateToProps
  & DispatchToProps
  & InjectedAuthRouterProps
  & RouteComponentProps<{organisation: string}>;

class LoginContainerComponent extends React.Component<Props, LoginState> {

  state: LoginState = {email: '', password: ''};

  render() {
    const {auth: {error}, match: {params: {organisation}}} = this.props;
    const {email, password} = this.state;
    return (
      <ColumnCenter className={classNames('LoginContainer')}>
        <Paper zDepth={5} className="LoginPaper">
          <RowCenter className="customerLogo">
            <Logo src={getLogoPath(organisation)}/>
          </RowCenter>
          <form onSubmit={this.onSubmit}>
            <TextFieldInput
              id="email"
              floatingLabelText={firstUpperTranslated('email')}
              fullWidth={true}
              hintText={firstUpperTranslated('your email address')}
              value={email}
              onChange={this.onChange}
              onKeyPress={this.onKeyPress}
            />
            <TextFieldInput
              id="password"
              className="TextField"
              floatingLabelText={firstUpperTranslated('password')}
              fullWidth={true}
              hintText={firstUpperTranslated('your password')}
              value={password}
              onChange={this.onChange}
              onKeyPress={this.onKeyPress}
              type="password"
            />
            <ButtonLogin
              fullWidth={true}
              type="submit"
            />
            {<ErrorMessage {...error}/>}
          </form>
        </Paper>
      </ColumnCenter>
    );
  }

  onChange = (event: any): void => this.setState({[event.target.id]: event.target.value});

  login = (): void => {
    const {email, password} = this.state;
    this.props.login(email, password);
  }

  onKeyPress = (event: any): void => {
    if (event.key === 'Enter') {
      event.preventDefault();
      this.login();
    }
  }

  onSubmit = (event: any): void => {
    event.preventDefault();
    this.login();
  }
}

const mapStateToProps = (state: RootState): StateToProps => ({auth: state.auth});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  login,
}, dispatch);

export const LoginContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(LoginContainerComponent);
