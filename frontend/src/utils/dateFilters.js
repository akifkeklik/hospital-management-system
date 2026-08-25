export const getTimeFilterParams = (timeFilter) => {
  if (!timeFilter || timeFilter === 'all') return {};
  
  const start = new Date();
  start.setHours(0, 0, 0, 0);
  
  const end = new Date();
  end.setHours(23, 59, 59, 999);
  
  if (timeFilter === 'week') end.setDate(end.getDate() + 7);
  else if (timeFilter === 'month') end.setDate(end.getDate() + 30);
  else if (timeFilter === '3months') end.setDate(end.getDate() + 90);
  else if (timeFilter === '6months') end.setDate(end.getDate() + 180);
  
  // Return ISO format suitable for Spring Boot @DateTimeFormat(iso=DATE_TIME)
  return {
    startDate: start.toISOString().split('.')[0],
    endDate: end.toISOString().split('.')[0]
  };
};
