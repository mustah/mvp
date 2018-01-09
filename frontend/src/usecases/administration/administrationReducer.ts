import {EndPoints} from '../../state/domain-models/domainModels';
import {DOMAIN_MODELS_CREATE_FAILURE} from '../../state/domain-models/domainModelsActions';
import {Action, ErrorResponse} from '../../types/Types';
import {AdministrationState} from './administrationModels';

const initialState: AdministrationState = {};

type InterestingActions = Action<ErrorResponse>;

export const administration =
  (state: AdministrationState = initialState, action: InterestingActions): AdministrationState => {
    switch (action.type) {
      case DOMAIN_MODELS_CREATE_FAILURE.concat(EndPoints.users):
        return {
          ...state,
          error: action.payload,
        };
    }
    return state;
  };
