import * as classNames from 'classnames';
import {Paper} from 'material-ui';
import TextField from 'material-ui/TextField';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {RootState} from '../../../reducers/rootReducer';
import {ColumnCenter} from '../../common/components/layouts/column/Column';
import {Logo} from '../../common/components/logo/Logo';
import {login} from '../authActions';
import {AuthState} from '../authReducer';
import './LoginContainer.scss';

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

    this.onChange = this.onChange.bind(this);
    this.onSubmit = this.onSubmit.bind(this);
  }

  onChange(event: any, ev2) {
    const {target: {id, value}} = event;
    this.setState({[id]: value});
  }

  onSubmit(event: any): void {
    event.preventDefault();
    const {email, password} = this.state;
    this.props.login(email, password);
  }

  render() {
    const {auth} = this.props;
    return (
      <ColumnCenter className={classNames('LoginContainer')}>
        <Paper zDepth={3} className="LoginPaper">
          <ColumnCenter className="customerLogo">
            <Logo/>
          </ColumnCenter>
          <form onSubmit={this.onSubmit}>
            <TextField
              className="TextField"
              floatingLabelText="Email"
              fullWidth={true}
              hintText="Din email-adress"
              id="email"
              onChange={this.onChange}
            />
            <TextField
              className="TextField"
              floatingLabelText="Lösenord"
              fullWidth={true}
              hintText="Ditt lösenord"
              id="password"
              onChange={this.onChange}
              type="password"
            />
            <div>
              <input type="submit" value="Login"/>
            </div>
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
