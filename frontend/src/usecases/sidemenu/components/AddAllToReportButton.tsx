import * as React from 'react';
import {ButtonLinkBlue} from '../../../components/buttons/ButtonLink';
import {translate} from '../../../services/translationService';
import {ClassNamed, Clickable} from '../../../types/Types';

export const AddAllToReportButton = (props: Clickable & ClassNamed) => (
  <ButtonLinkBlue {...props}>
    {translate('add all to report')}
  </ButtonLinkBlue>
);
