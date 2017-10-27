import * as classNames from 'classnames';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {Logo} from '../../common/components/logo/Logo';
import {Column} from '../../common/components/layouts/column/Column';
import {login} from '../authActions';
import {AuthState} from '../authReducer';
import './LoginContainer.scss';

interface StateToProps {
  auth: AuthState;
}

interface DispatchToProps {
  login: (email: string, password: string) => any;
}

type Props = StateToProps & DispatchToProps & InjectedAuthRouterProps;

class LoginContainerComponent extends React.Component<Props> {

  private emailComponent: HTMLInputElement | null;
  private passwordComponent: HTMLInputElement | null;

  render() {
    const {auth} = this.props;
    return (
      <Column className={classNames('LoginContainer', 'Column-center')}>
        <div className="customerLogo">
          <Logo/>
        </div>
        <form onSubmit={this.onSubmit}>
          <div>
            <input
              type="text"
              placeholder={translate('email')}
              ref={component => this.emailComponent = component}
            />
          </div>
          <div>
            <input
              type="password"
              placeholder={translate('password')}
              ref={component => this.passwordComponent = component}
            />
          </div>
          <div>
            <input type="submit" value="Login"/>
          </div>
          {auth.error && <div className="error-message">{auth.error.error}: {auth.error.message}</div>}
        </form>
      </Column>
    );
  }

  onSubmit = (event: any): void => {
    event.preventDefault();
    this.props.login(this.emailComponent!.value, this.passwordComponent!.value);
  }
}

const mapStateToProps = (state: RootState): StateToProps => ({auth: state.auth});

const mapDispatchToProps = dispatch => bindActionCreators({
  login,
}, dispatch);

export const LoginContainer =
  connect<StateToProps, DispatchToProps, {}>(mapStateToProps, mapDispatchToProps)(LoginContainerComponent);
