import {default as classNames} from 'classnames';
import {Paper} from 'material-ui';
import FlatButton from 'material-ui/FlatButton';
import * as React from 'react';
import {connect} from 'react-redux';
import {RouteComponentProps} from 'react-router';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {getLoginLogoPath} from '../../../app/routes';
import {buttonStyle} from '../../../app/themes';
import {ErrorMessage} from '../../../components/error-message/ErrorMessage';
import {TextFieldInput} from '../../../components/inputs/TextFieldInput';
import {ColumnCenter} from '../../../components/layouts/column/Column';
import {RowCenter} from '../../../components/layouts/row/Row';
import {Logo} from '../../../components/logo/Logo';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {login} from '../authActions';
import {AuthState} from '../authModels';
import './LoginContainer.scss';

interface StateToProps {
  auth: AuthState;
}

interface DispatchToProps {
  login: (email: string, password: string) => void;
}

interface State {
  email: string;
  password: string;
}

type Props =
  & StateToProps
  & DispatchToProps
  & InjectedAuthRouterProps
  & RouteComponentProps<{organisation: string}>;

const LoginContainerComponent = ({auth: {error}, match: {params: {organisation}}, login}: Props) => {
  const [state, setState] = React.useState<State>({email: '', password: ''});

  const onChangeEmail = (event: any): void => setState({...state, email: event.target.value});
  const onChangePassword = (event: any): void => setState({...state, password: event.target.value});

  const onKeyPress = (event: any): void => {
    if (event.key === 'Enter') {
      event.preventDefault();
      login(state.email, state.password);
    }
  };

  const onSubmit = (event: any): void => {
    event.preventDefault();
    login(state.email, state.password);
  };

  return (
    <ColumnCenter className={classNames('LoginContainer')}>
      <Paper zDepth={5} className="LoginPaper">
        <RowCenter className="customerLogo">
          <Logo className="login" src={getLoginLogoPath(organisation)}/>
        </RowCenter>
        <form onSubmit={onSubmit}>
          <TextFieldInput
            id="email"
            floatingLabelText={firstUpperTranslated('email')}
            fullWidth={true}
            hintText={firstUpperTranslated('your email address')}
            value={state.email}
            onChange={onChangeEmail}
            onKeyPress={onKeyPress}
          />
          <TextFieldInput
            id="password"
            className="TextField"
            floatingLabelText={firstUpperTranslated('password')}
            fullWidth={true}
            hintText={firstUpperTranslated('your password')}
            value={state.password}
            onChange={onChangePassword}
            onKeyPress={onKeyPress}
            type="password"
          />
          <FlatButton
            fullWidth={true}
            label={translate('login')}
            style={buttonStyle}
            type="submit"
          />
          <ErrorMessage {...error} style={{marginTop: 16}}/>
        </form>
      </Paper>
    </ColumnCenter>
  );
};

const mapStateToProps = (state: RootState): StateToProps => ({auth: state.auth});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  login,
}, dispatch);

export const LoginContainer =
  connect<StateToProps, DispatchToProps>(
    mapStateToProps,
    mapDispatchToProps,
  )(LoginContainerComponent);
