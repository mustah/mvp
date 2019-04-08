import {firstUpperTranslated} from '../../../services/translationService';
import {IdNamed, Omit, uuid} from '../../../types/Types';

export interface Organisation extends IdNamed {
  slug: string;
  parent?: Organisation;
  selectionId?: uuid;
}

export type OrganisationWithoutId = Omit<Organisation, 'id'>;

export const noOrganisationId = -1;

export const noOrganisation = (): Organisation => ({
  id: noOrganisationId,
  name: firstUpperTranslated('no parent organisation'),
  slug: 'none',
});
