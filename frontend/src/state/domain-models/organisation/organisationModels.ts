import {firstUpperTranslated} from '../../../services/translationService';
import {IdNamed, Omit, uuid} from '../../../types/Types';

export interface Organisation extends IdNamed {
  slug: string;
  parent?: Organisation;
  selectionId?: uuid;
}

export type OrganisationWithoutId = Omit<Organisation, 'id'>;

export const noOrganisation = (): Organisation => ({
  id: -1,
  name: firstUpperTranslated('no parent organisation'),
  slug: 'none',
});
