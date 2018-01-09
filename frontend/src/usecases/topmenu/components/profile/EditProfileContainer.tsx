import 'EditProfileContainer.scss';
import FlatButton from 'material-ui/FlatButton';
import Paper from 'material-ui/Paper';
import TextField from 'material-ui/TextField';
import * as React from 'react';
import {connect} from 'react-redux';
import {buttonStyle, paperStyle} from '../../../../app/themes';
import {Column} from '../../../../components/layouts/column/Column';
import {Row} from '../../../../components/layouts/row/Row';
import {MainTitle} from '../../../../components/texts/Titles';
import {MvpPageContainer} from '../../../../containers/MvpPageContainer';
import {RootState} from '../../../../reducers/rootReducer';
import {translate} from '../../../../services/translationService';
import {User} from '../../../../state/domain-models/user/userModels';

interface StateToProps {
  user: User;
}

type Props = StateToProps;

const EditProfile = ({user}: Props) => {

  const onSubmit = (event) => {
    event.preventDefault();
    // TODO: Create submit request to backend.
  };

  // TODO: Add validation for fields.
  return (
    <MvpPageContainer>
      <Row className="space-between">
        <MainTitle>
          {translate('profile')}
        </MainTitle>
      </Row>
      <Paper style={paperStyle}>
        <Column className="EditProfileContainer">
          <form onSubmit={onSubmit}>
            <Column>
              <TextField
                className="TextField"
                type="text"
                defaultValue={user.name}
                name="name"
                hintText={translate('name')}
                floatingLabelText={translate('name')}
              />

              <TextField
                className="TextField"
                type="text"
                defaultValue={user.organisation.name}
                name="name"
                hintText={translate('organisation')}
                floatingLabelText={translate('organisation')}
                disabled={true}
              />

              <TextField
                className="TextField"
                type="password"
                name="currentPassword"
                hintText={translate('current password')}
                floatingLabelText={translate('current password')}
              />

              <TextField
                className="TextField"
                type="password"
                name="newPassword"
                hintText={translate('new password')}
                floatingLabelText={translate('new password')}
              />

              <TextField
                className="TextField"
                type="text"
                defaultValue={user.email}
                name="name"
                hintText={translate('email')}
                floatingLabelText={translate('email')}
              />

              <TextField
                className="TextField"
                type="text"
                defaultValue={user.roles.toString()}
                name="name"
                hintText={translate('roles')}
                floatingLabelText={translate('roles')}
                disabled={true}
              />
              <FlatButton
                className="ProfileSave"
                type="submit"
                label={translate('save')}
                style={buttonStyle}
              />
            </Column>
          </form>
        </Column>
      </Paper>
    </MvpPageContainer>
  );
};

const mapStateToProps = ({auth: {user}}: RootState): StateToProps => (
  {user: user!} // TODO: Perhaps use a selector instead of using the "!" null protection.
);

export const EditProfileContainer = connect<StateToProps>(mapStateToProps)(EditProfile);