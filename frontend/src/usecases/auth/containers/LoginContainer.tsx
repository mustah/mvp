import * as classNames from 'classnames';
import {Paper} from 'material-ui';
import FlatButton from 'material-ui/FlatButton';
import TextField from 'material-ui/TextField';
import * as React from 'react';
import {connect} from 'react-redux';
import {RouteComponentProps} from 'react-router';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {colors, floatingLabelFocusStyle, underlineFocusStyle} from '../../../app/themes';
import {ColumnCenter} from '../../../components/layouts/column/Column';
import {RowCenter} from '../../../components/layouts/row/Row';
import {Logo} from '../../../components/logo/Logo';
import {LogoCompanySpecific} from '../../../components/logo/LogoCompanySpecfic';
import {RootState} from '../../../reducers/rootReducer';
import {login} from '../authActions';
import {AuthState} from '../authModels';
import './LoginContainer.scss';

const loginButtonStyle = {
  backgroundColor: colors.blue,
  color: '#fff',
};

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

type Props = StateToProps & DispatchToProps & InjectedAuthRouterProps & RouteComponentProps<{company: string}>;

class LoginContainerComponent extends React.Component<Props, LoginState> {

  state: LoginState = {email: '', password: ''};

  render() {
    const {auth, match: {params: {company}}} = this.props;
    const logo = company ? <LogoCompanySpecific company={company}/> : <Logo />;
    return (
      <ColumnCenter className={classNames('LoginContainer')}>
        <Paper zDepth={5} className="LoginPaper">
          <RowCenter className="customerLogo">
            {logo}
          </RowCenter>
          <form onSubmit={this.onSubmit}>
            <TextField
              className="TextField"
              floatingLabelText="Email"
              floatingLabelFocusStyle={floatingLabelFocusStyle}
              fullWidth={true}
              hintText="Din email-adress"
              id="email"
              onChange={this.onChange}
              onKeyPress={this.onKeyPress}
              underlineFocusStyle={underlineFocusStyle}
            />
            <TextField
              className="TextField"
              floatingLabelText="Lösenord"
              floatingLabelFocusStyle={floatingLabelFocusStyle}
              fullWidth={true}
              hintText="Ditt lösenord"
              id="password"
              onChange={this.onChange}
              onKeyPress={this.onKeyPress}
              type="password"
              underlineFocusStyle={underlineFocusStyle}
            />
            <FlatButton
              fullWidth={true}
              label="Logga in"
              onClick={this.onSubmit}
              style={loginButtonStyle}
            />
            {auth.error && <div className="error-message">{auth.error.error}: {auth.error.message}</div>}
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
