import * as React from 'react';
import {ButtonLink} from '../../../../components/buttons/ButtonLink';
import {translate} from '../../../../services/translationService';
import {SelectionMenuProps} from './SelectionMenuSummary';

export const ResetSelectionButton = ({resetSelection}: SelectionMenuProps) =>
  <ButtonLink onClick={resetSelection}>{translate('reset selection')}</ButtonLink>;

export const DiscardChangesButton = ({selectSavedSelection}: SelectionMenuProps) =>
  <ButtonLink onClick={selectSavedSelection}>{translate('discard changes')}</ButtonLink>;
