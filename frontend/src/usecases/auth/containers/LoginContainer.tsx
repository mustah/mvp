import * as classNames from 'classnames';
import {Paper} from 'material-ui';
import FlatButton from 'material-ui/FlatButton';
import TextField from 'material-ui/TextField';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {RootState} from '../../../reducers/rootReducer';
import {colors, floatingLabelFocusStyle, paperStyle, underlineFocusStyle} from '../../app/themes';
import {ColumnCenter} from '../../common/components/layouts/column/Column';
import {Logo} from '../../common/components/logo/Logo';
import {login} from '../authActions';
import {AuthState} from '../authReducer';
import './LoginContainer.scss';

const loginButtonStyle = {
  backgroundColor: colors.blue,
  color: '#fff',
};

interface StateToProps {
  auth: AuthState;
}

interface DispatchToProps {
  login: (email: string, password: string) => any;
}

interface LoginState {
  email: string;
  password: string;
}

type Props = StateToProps & DispatchToProps & InjectedAuthRouterProps;

class LoginContainerComponent extends React.Component<Props, LoginState> {

  constructor(props) {
    super(props);
    this.state = {email: '', password: ''};
  }

  onChange = (event: any): void => {
    const {target: {id, value}} = event;
    this.setState({[id]: value});
  }

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

  render() {
    const {auth} = this.props;

    return (
      <ColumnCenter className={classNames('LoginContainer')}>
        <Paper className="LoginPaper" style={paperStyle}>
          <ColumnCenter className="customerLogo">
            <Logo/>
          </ColumnCenter>
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
}

const mapStateToProps = (state: RootState): StateToProps => ({auth: state.auth});

const mapDispatchToProps = dispatch => bindActionCreators({
  login,
}, dispatch);

export const LoginContainer =
  connect<StateToProps, DispatchToProps, {}>(mapStateToProps, mapDispatchToProps)(LoginContainerComponent);
