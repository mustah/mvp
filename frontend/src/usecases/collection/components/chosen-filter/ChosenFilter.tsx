import {Chip} from 'material-ui';
import * as React from 'react';
import {translate} from '../../../../services/translationService';
import {Bold} from '../../../common/components/texts/Texts';
import './ChosenFilter.scss';

interface ChosenFilterProps {
  filter: any;
  onDelete: (category: string, value: string) => any;
}

export const ChosenFilter = (props: ChosenFilterProps) => {
  const {filter, onDelete} = props;
  if (Object.keys(filter).length === 0) {
    return null;
  }

  const chips: any = [];
  Object.keys(filter).forEach((filterCategory, index) => {
    const filterSpecificDeletion = () => {
      return onDelete(filterCategory, filter[filterCategory]);
    };
    // TODO replace the "I think every value in the filter object is a string" with typing,
    // when we set the real format of filter.. because this will
    // blow up if typeof filter[key] !== 'string'
    chips.push((
      <Chip key={index} onRequestDelete={filterSpecificDeletion}>
        {filterCategory}: {filter[filterCategory]}
      </Chip>
    ));
  });

  return (
    <div>
      <Bold className="CurrentFilter">{translate('current filter')}:</Bold>
      {chips}
    </div>
  );
};
