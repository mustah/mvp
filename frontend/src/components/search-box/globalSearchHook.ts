import * as React from 'react';
import {routes} from '../../app/routes';
import {GlobalSearchProps} from '../../containers/GlobalSearchContainer';
import {history} from '../../index';
import {OnChange, OnClick, OnKeyPress} from '../../types/Types';

interface GlobalSearch {
  value?: string;
  onChange: OnChange;
  onEnter: OnKeyPress;
  onClearValue: OnClick;
}

export const useGlobalSearch = ({onSearch, onClear, query = ''}: GlobalSearchProps): GlobalSearch => {
  const [value, setValue] = React.useState<string>(query);

  const onEnter = (event) => {
    if (event.key === 'Enter') {
      event.preventDefault();
      const value = event.target.value;
      setValue(value);
      onSearch(value);
      history.push(routes.searchResult);
    }
  };

  const onClearValue = () => {
    setValue('');
    onClear('');
  };

  const onChange = (event) => {
    event.preventDefault();
    setValue(event.target.value);
  };

  return {value, onChange, onClearValue, onEnter};
};
