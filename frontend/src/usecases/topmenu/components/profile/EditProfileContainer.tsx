import 'EditProfileContainer.scss';
import TextField from 'material-ui/TextField';
import * as React from 'react';
import {connect} from 'react-redux';
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
            />

            <TextField
              className="TextField"
              type="password"
              defaultValue={'****'}
              name="currentPassword"
              hintText={translate('current password')}
              floatingLabelText={translate('current password')}
            />

            <TextField
              className="TextField"
              type="password"
              defaultValue={'****'}
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
              disabled={true}
              hintText={translate('roles')}
              floatingLabelText={translate('roles')}
            />
            <input type="submit" className="ProfileSave clickable" value={translate('save')}/>
          </Column>
        </form>
      </Column>
    </MvpPageContainer>
  );
};

const mapStateToProps = ({auth: {user}}: RootState): StateToProps => (
  {user: user!} // TODO: Perhaps use a selector instead of using the "!" null protection.
);

export const EditProfileContainer = connect<StateToProps>(mapStateToProps)(EditProfile);
