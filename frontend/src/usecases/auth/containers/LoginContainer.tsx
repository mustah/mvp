import * as classNames from 'classnames';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {RootState} from '../../../reducers/index';
import {translate} from '../../../services/translationService';
import {Column} from '../../layouts/components/column/Column';
import {login} from '../authActions';
import {AuthState} from '../authReducer';
import './LoginContainer.scss';

export interface LoginProps {
  login: (email: string, password: string) => any;
  auth: AuthState;
}

class LoginContainer extends React.Component<LoginProps & InjectedAuthRouterProps> {

  private emailComponent: HTMLInputElement | null;
  private passwordComponent: HTMLInputElement | null;

  login = () => {
    this.props.login(this.emailComponent!.value, this.passwordComponent!.value);
  }

  render() {
    const {auth} = this.props;
    return (
      <Column className={classNames('LoginContainer', 'Column-center')}>
        <form onSubmit={this.login}>
          <div>
            <input type="text" placeholder={translate('email')} ref={component => this.emailComponent = component}/>
          </div>
          <div>
            <input
              type="password"
              placeholder={translate('password')}
              ref={component => this.passwordComponent = component}
            />
          </div>
          <div>
            <input type="submit" onClick={this.login} value="Login"/>
          </div>
          {auth.error && <div className="error-message">{auth.error.error}: {auth.error.message}</div>}
        </form>
      </Column>
    );
  }

}

const mapStateToProps = (state: RootState) => ({auth: state.auth});

const mapDispatchToProps = dispatch => bindActionCreators({
  login,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(LoginContainer);
