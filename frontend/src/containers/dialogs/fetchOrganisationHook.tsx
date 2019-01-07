import * as React from 'react';
import {User} from '../../state/domain-models/user/userModels';
import {isSuperAdmin} from '../../state/domain-models/user/userSelectors';
import {CallbackWithId, uuid} from '../../types/Types';

interface Props {
  fetchOrganisation: CallbackWithId;
  organisationId: uuid;
  user: User;
}

export const useFetchOrganisation = ({fetchOrganisation, organisationId, user}: Props): void => {
  React.useEffect(() => {
    if (isSuperAdmin(user)) {
      fetchOrganisation(organisationId);
    }
  }, [organisationId, user]);
};
