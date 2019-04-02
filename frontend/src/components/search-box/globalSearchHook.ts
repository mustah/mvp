import * as React from 'react';
import {history, routes} from '../../app/routes';
import {GlobalSearchProps} from '../../containers/GlobalSearchContainer';
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
    const value = event.target.value;
    if (event.key === 'Enter') {
      event.preventDefault();
      setValue(value);
      onSearch(value);
      history.push(`${routes.searchResult}/${encodeURIComponent(value)}`);
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
