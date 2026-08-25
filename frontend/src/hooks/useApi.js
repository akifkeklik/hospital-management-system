import { useState, useCallback, useRef, useEffect } from 'react';

/**
 * Custom hook to manage safe API request lifecycles.
 * Features:
 * - AbortController integration for cancelling previous requests
 * - Stale response protection
 * - Loading and error state management
 * 
 * @param {Function} apiFunc The async function to execute. First argument must be the AbortSignal.
 * @param {any} initialData Initial data state.
 */
export function useApi(apiFunc, initialData = null) {
  const [data, setData] = useState(initialData);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // Keep track of the active request's AbortController
  const abortControllerRef = useRef(null);

  const execute = useCallback(async (...args) => {
    // Cancel the previous request if it's still running
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }

    // Create a new AbortController for the current request
    const abortController = new AbortController();
    abortControllerRef.current = abortController;

    setLoading(true);
    setError(null);

    try {
      // Execute the API function, passing the signal as the first argument
      const result = await apiFunc(abortController.signal, ...args);
      
      // Stale Response Protection: Only update state if this request wasn't superseded by a newer one
      if (abortControllerRef.current === abortController) {
        setData(result);
        setLoading(false);
      }
      return result;
    } catch (err) {
      if (err.name === 'AbortError') {
        // This is a cancellation. The browser/frontend fetch request was aborted by AbortController.
        // We do NOT update the error state, and we do NOT set loading to false,
        // because the new request (which triggered this abort) is now handling the state.
        console.log('Request cancelled by AbortController');
      } else {
        // Stale Response Protection for errors
        if (abortControllerRef.current === abortController) {
          setError(err.message || 'An error occurred');
          setLoading(false);
        }
      }
      // Re-throw if it's a real error so the caller can handle it if needed
      if (err.name !== 'AbortError') {
        throw err;
      }
    }
  }, [apiFunc]);

  // Cleanup on component unmount
  useEffect(() => {
    return () => {
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
    };
  }, []);

  return { data, setData, loading, error, execute };
}
