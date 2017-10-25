import {Chip} from 'material-ui';
import * as React from 'react';
import {translate} from '../../../../services/translationService';
import {Bold} from '../../../common/components/texts/Texts';
import {Filter} from '../../models/Collections';
import './ChosenFilter.scss';
import {uuid} from '../../../../types/Types';

interface ChosenFilterProps {
  filter: Filter;
  onDelete: (category: string, value: uuid) => any;
}

export const ChosenFilter = (props: ChosenFilterProps) => {
  const {filter, onDelete} = props;
  if (Object.keys(filter).length === 0) {
    return null;
  }

  const chips: any = [];
  Object.keys(filter).forEach((filterCategory) => {
    filter[filterCategory].forEach((value) => {
      const filterSpecificDeletion = () => {
        return onDelete(filterCategory, value);
      };
      chips.push((
        <Chip key={filterCategory + '-' + value} onRequestDelete={filterSpecificDeletion}>
          {filterCategory}: {value}
        </Chip>
      ));
    });
  });

  return (
    <div>
      <Bold className="CurrentFilter">{translate('current filter')}:</Bold>
      {chips}
    </div>
  );
};
