import * as React from 'react';
import {connect} from 'react-redux';
import {Column} from '../../../../components/layouts/column/Column';
import {Row} from '../../../../components/layouts/row/Row';
import {Bold} from '../../../../components/texts/Texts';
import {MainTitle} from '../../../../components/texts/Titles';
import {MvpPageContainer} from '../../../../containers/MvpPageContainer';
import {RootState} from '../../../../reducers/rootReducer';
import {translate} from '../../../../services/translationService';
import {User} from '../../../../state/domain-models/user/userModels';
import 'EditProfileContainer.scss';

interface StateToProps {
  user: User;
}

type Props = StateToProps;

const EditProfile = ({user}: Props) => {

  const onSubmit = () => console.log('Submitting');

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
            <Bold className="first-uppercase">{translate('name')}:</Bold>
            <input type="text" defaultValue={user.name} name="name"/>

            <Bold className="first-uppercase">{translate('organisation')}:</Bold>
            <input type="text" defaultValue={user.organisation.name} name="name"/>

            <Bold className="first-uppercase">{translate('email')}:</Bold>
            <input type="text" defaultValue={user.email} name="name"/>

            <Bold className="first-uppercase">{translate('roles')}:</Bold>
            <input type="text" defaultValue={user.roles} name="name" disabled={true}/>
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
